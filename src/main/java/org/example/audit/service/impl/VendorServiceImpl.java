package org.example.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.audit.enums.AuditNodeTypeEnum;
import org.example.audit.enums.AuditResultEnum;
import org.example.audit.enums.AuditTypeEnum;
import org.example.audit.enums.VendorStatusEnum;
import org.example.audit.dto.SubmitVendorDTO;
import org.example.audit.mapper.*;
import org.example.audit.po.*;
import org.example.audit.service.VendorService;
import org.example.audit.vo.*;
import org.example.auth.common.PcUserInfo;
import org.example.auth.common.UserContext;
import org.example.auth.constants.HttpStatusConstants;
import org.example.auth.enums.PcUserIdentityEnum;
import org.example.auth.vo.HttpResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 厂商服务实现（厂商用户端）
 * @author fasonghao
 */
@Service
public class VendorServiceImpl implements VendorService {

    @Autowired
    private VendorMapper vendorMapper;

    @Autowired
    private VendorUserRelationMapper vendorUserRelationMapper;

    @Autowired
    private VendorAuditRecordMapper vendorAuditRecordMapper;

    @Autowired
    private AuditInstanceMapper auditInstanceMapper;

    @Autowired
    private AuditNodeMapper auditNodeMapper;

    @Autowired
    private AuditMapStructMapper mapStructMapper;

    /**
     * 保存草稿
     */
    @Override
    @Transactional
    public HttpResponseVO<Integer> saveDraft(SubmitVendorDTO dto) {
        PcUserInfo userInfo = UserContext.get();

        if (dto.getVendorId() != null) {
            // 编辑已有草稿
            VendorPO vendor = vendorMapper.selectById(dto.getVendorId());
            if (vendor == null) {
                return HttpResponseVO.<Integer>builder().code(HttpStatusConstants.ERROR).msg("厂商不存在").build();
            }
            if (vendor.getStatus() != VendorStatusEnum.DRAFT && vendor.getStatus() != VendorStatusEnum.REJECTED) {
                return HttpResponseVO.<Integer>builder().code(HttpStatusConstants.ERROR).msg("当前状态不允许编辑").build();
            }
            if (!isVendorUser(userInfo.getUserId(), dto.getVendorId())) {
                return HttpResponseVO.<Integer>builder().code(HttpStatusConstants.ERROR).msg("无权操作该厂商").build();
            }
            copyDtoToVendor(dto, vendor);
            vendor.setStatus(VendorStatusEnum.DRAFT);
            vendorMapper.updateById(vendor);
            return HttpResponseVO.<Integer>builder().data(vendor.getVendorId()).code(HttpStatusConstants.SUCCESS).msg("草稿保存成功").build();
        }

        // 新建草稿
        VendorPO vendor = new VendorPO();
        copyDtoToVendor(dto, vendor);
        vendor.setStatus(VendorStatusEnum.DRAFT);
        vendorMapper.insert(vendor);

        // 创建关联关系
        VendorUserRelationPO relation = new VendorUserRelationPO();
        relation.setVendorUserId(userInfo.getUserId());
        relation.setVendorId(vendor.getVendorId());
        relation.setIsMain(true);
        vendorUserRelationMapper.insert(relation);

        return HttpResponseVO.<Integer>builder().data(vendor.getVendorId()).code(HttpStatusConstants.SUCCESS).msg("草稿保存成功").build();
    }

    /**
     * 提交入驻申请
     */
    @Override
    @Transactional
    public HttpResponseVO<Integer> submitApplication(SubmitVendorDTO dto) {
        PcUserInfo userInfo = UserContext.get();

        if (dto.getVendorId() != null) {
            // 提交已有草稿
            VendorPO vendor = vendorMapper.selectById(dto.getVendorId());
            if (vendor == null) {
                return HttpResponseVO.<Integer>builder().code(HttpStatusConstants.ERROR).msg("厂商不存在").build();
            }
            if (vendor.getStatus() != VendorStatusEnum.DRAFT) {
                return HttpResponseVO.<Integer>builder().code(HttpStatusConstants.ERROR).msg("当前状态不允许提交").build();
            }
            if (!isVendorUser(userInfo.getUserId(), dto.getVendorId())) {
                return HttpResponseVO.<Integer>builder().code(HttpStatusConstants.ERROR).msg("无权操作该厂商").build();
            }
            copyDtoToVendor(dto, vendor);
            vendor.setStatus(VendorStatusEnum.PENDING);
            vendor.setSubmittedTime(LocalDateTime.now());
            vendorMapper.updateById(vendor);
            createAuditRecord(vendor.getVendorId(), 1, AuditTypeEnum.INITIAL, dto);
            return HttpResponseVO.<Integer>builder().data(vendor.getVendorId()).code(HttpStatusConstants.SUCCESS).msg("入驻申请提交成功").build();
        }

        // 直接新建并提交
        VendorPO vendor = new VendorPO();
        copyDtoToVendor(dto, vendor);
        vendor.setStatus(VendorStatusEnum.PENDING);
        vendor.setSubmittedTime(LocalDateTime.now());
        vendorMapper.insert(vendor);

        VendorUserRelationPO relation = new VendorUserRelationPO();
        relation.setVendorUserId(userInfo.getUserId());
        relation.setVendorId(vendor.getVendorId());
        relation.setIsMain(true);
        vendorUserRelationMapper.insert(relation);

        createAuditRecord(vendor.getVendorId(), 1, AuditTypeEnum.INITIAL, dto);
        return HttpResponseVO.<Integer>builder().data(vendor.getVendorId()).code(HttpStatusConstants.SUCCESS).msg("入驻申请提交成功").build();
    }

    /**
     * 驳回后重新提交
     */
    @Override
    @Transactional
    public HttpResponseVO<String> resubmit(Integer vendorId, SubmitVendorDTO dto) {
        PcUserInfo userInfo = UserContext.get();
        if (userInfo.getRole() != PcUserIdentityEnum.VENDOR_USER) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("仅厂商用户可操作")
                    .build();
        }

        VendorPO vendor = vendorMapper.selectById(vendorId);
        if (vendor == null) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("厂商不存在")
                    .build();
        }

        if (vendor.getStatus() != VendorStatusEnum.REJECTED) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("当前状态不允许重新提交")
                    .build();
        }

        // 验证当前用户是否为该厂商的用户
        if (!isVendorUser(userInfo.getUserId(), vendorId)) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("无权操作该厂商")
                    .build();
        }

        // 更新厂商信息
        copyDtoToVendor(dto, vendor);
        vendor.setStatus(VendorStatusEnum.PENDING);
        vendor.setSubmittedTime(LocalDateTime.now());
        vendorMapper.updateById(vendor);

        // 获取当前最大轮次
        LambdaQueryWrapper<VendorAuditRecordPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(VendorAuditRecordPO::getVendorId, vendorId);
        wrapper.orderByDesc(VendorAuditRecordPO::getRound);
        wrapper.last("LIMIT 1");
        VendorAuditRecordPO lastRecord = vendorAuditRecordMapper.selectOne(wrapper);
        int nextRound = (lastRecord != null) ? lastRecord.getRound() + 1 : 1;

        // 创建新一轮审核记录
        createAuditRecord(vendorId, nextRound, AuditTypeEnum.INITIAL, dto);

        return HttpResponseVO.<String>builder()
                .code(HttpStatusConstants.SUCCESS)
                .msg("重新提交成功")
                .build();
    }

    /**
     * 获取当前厂商用户关联的厂商信息
     */
    @Override
    public HttpResponseVO<VendorVO> getMyVendorInfo() {
        PcUserInfo userInfo = UserContext.get();
        if (userInfo.getRole() != PcUserIdentityEnum.VENDOR_USER) {
            return HttpResponseVO.<VendorVO>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("仅厂商用户可操作")
                    .build();
        }

        VendorPO vendor = getVendorByUserId(userInfo.getUserId());
        if (vendor == null) {
            return HttpResponseVO.<VendorVO>builder()
                    .code(HttpStatusConstants.SUCCESS)
                    .msg("暂未关联厂商")
                    .build();
        }

        return HttpResponseVO.<VendorVO>builder()
                .data(mapStructMapper.vendorPoToVo(vendor))
                .code(HttpStatusConstants.SUCCESS)
                .msg("获取厂商信息成功")
                .build();
    }

    /**
     * 获取审核进度
     */
    @Override
    public HttpResponseVO<AuditProgressVO> getAuditProgress(Integer vendorId) {
        PcUserInfo userInfo = UserContext.get();
        // 厂商用户需验证关联关系，管理员可直接查看
        if (userInfo.getRole() == PcUserIdentityEnum.VENDOR_USER
                && !isVendorUser(userInfo.getUserId(), vendorId)) {
            return HttpResponseVO.<AuditProgressVO>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("无权查看该厂商审核进度")
                    .build();
        }

        VendorPO vendor = vendorMapper.selectById(vendorId);
        if (vendor == null) {
            return HttpResponseVO.<AuditProgressVO>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("厂商不存在")
                    .build();
        }

        // 获取最新一轮审核记录
        LambdaQueryWrapper<VendorAuditRecordPO> recordWrapper = Wrappers.lambdaQuery();
        recordWrapper.eq(VendorAuditRecordPO::getVendorId, vendorId);
        recordWrapper.orderByDesc(VendorAuditRecordPO::getRound);
        recordWrapper.last("LIMIT 1");
        VendorAuditRecordPO latestRecord = vendorAuditRecordMapper.selectOne(recordWrapper);

        AuditProgressVO progressVO = new AuditProgressVO();
        progressVO.setVendorStatus(vendor.getStatus().getValue());

        if (latestRecord != null) {
            progressVO.setCurrentRound(latestRecord.getRound());
            progressVO.setAuditRecord(mapStructMapper.auditRecordPoToVo(latestRecord));

            // 获取该轮次的流程实例列表
            LambdaQueryWrapper<AuditInstancePO> instanceWrapper = Wrappers.lambdaQuery();
            instanceWrapper.eq(AuditInstancePO::getAuditRecordId, latestRecord.getAuditRecordId());
            instanceWrapper.orderByAsc(AuditInstancePO::getAuditNodeId);
            List<AuditInstancePO> instances = auditInstanceMapper.selectList(instanceWrapper);

            List<AuditInstanceVO> instanceVOs = new ArrayList<>();
            for (AuditInstancePO inst : instances) {
                AuditInstanceVO vo = new AuditInstanceVO();
                vo.setAuditInstanceId(inst.getAuditInstanceId());
                vo.setVendorId(inst.getVendorId());
                vo.setAuditRecordId(inst.getAuditRecordId());
                vo.setAuditNodeId(inst.getAuditNodeId());
                vo.setCreatedTime(inst.getCreatedTime());
                vo.setStartedTime(inst.getStartedTime());
                vo.setCompletedTime(inst.getCompletedTime());
                // 查询节点信息
                AuditNodePO node = auditNodeMapper.selectById(inst.getAuditNodeId());
                if (node != null) {
                    vo.setNodeName(node.getName());
                    vo.setNodeType(node.getType().getValue());
                }
                instanceVOs.add(vo);
            }
            progressVO.setInstances(instanceVOs);
        }

        return HttpResponseVO.<AuditProgressVO>builder()
                .data(progressVO)
                .code(HttpStatusConstants.SUCCESS)
                .msg("获取审核进度成功")
                .build();
    }

    /**
     * 获取当前厂商用户关联的所有厂商列表
     */
    @Override
    public HttpResponseVO<List<VendorVO>> getMyVendors() {
        PcUserInfo userInfo = UserContext.get();

        LambdaQueryWrapper<VendorUserRelationPO> relWrapper = Wrappers.lambdaQuery();
        relWrapper.eq(VendorUserRelationPO::getVendorUserId, userInfo.getUserId());
        List<VendorUserRelationPO> relations = vendorUserRelationMapper.selectList(relWrapper);

        if (relations == null || relations.isEmpty()) {
            return HttpResponseVO.<List<VendorVO>>builder()
                    .data(List.of())
                    .code(HttpStatusConstants.SUCCESS)
                    .msg("暂无关联厂商")
                    .build();
        }

        List<Integer> vendorIds = relations.stream()
                .map(VendorUserRelationPO::getVendorId)
                .collect(Collectors.toList());
        List<VendorPO> vendors = vendorMapper.selectBatchIds(vendorIds);

        List<VendorVO> voList = vendors.stream()
                .map(mapStructMapper::vendorPoToVo)
                .collect(Collectors.toList());

        return HttpResponseVO.<List<VendorVO>>builder()
                .data(voList)
                .code(HttpStatusConstants.SUCCESS)
                .msg("获取厂商列表成功")
                .build();
    }

    // ========== 私有方法 ==========

    /**
     * 通过用户ID查找关联的厂商（主管理员）
     */
    private VendorPO getVendorByUserId(Integer userId) {
        LambdaQueryWrapper<VendorUserRelationPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(VendorUserRelationPO::getVendorUserId, userId);
        wrapper.eq(VendorUserRelationPO::getIsMain, true);
        VendorUserRelationPO relation = vendorUserRelationMapper.selectOne(wrapper);
        if (relation == null) {
            return null;
        }
        return vendorMapper.selectById(relation.getVendorId());
    }

    /**
     * 验证用户是否为指定厂商的用户
     */
    private boolean isVendorUser(Integer userId, Integer vendorId) {
        LambdaQueryWrapper<VendorUserRelationPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(VendorUserRelationPO::getVendorUserId, userId);
        wrapper.eq(VendorUserRelationPO::getVendorId, vendorId);
        return vendorUserRelationMapper.selectCount(wrapper) > 0;
    }

    /**
     * 将DTO数据复制到PO
     */
    private void copyDtoToVendor(SubmitVendorDTO dto, VendorPO vendor) {
        vendor.setCompanyName(dto.getCompanyName());
        vendor.setShortName(dto.getShortName());
        vendor.setLicenseNo(dto.getLicenseNo());
        vendor.setLicenseImage(dto.getLicenseImage());
        vendor.setLegalPerson(dto.getLegalPerson());
        vendor.setLegalPersonId(dto.getLegalPersonId());
        vendor.setContactPerson(dto.getContactPerson());
        vendor.setContactPhone(dto.getContactPhone());
        vendor.setContactEmail(dto.getContactEmail());
        vendor.setCompanyAddress(dto.getCompanyAddress());
        vendor.setWebsite(dto.getWebsite());
        vendor.setIntroduction(dto.getIntroduction());
        vendor.setBusinessScope(dto.getBusinessScope());
        vendor.setApiEndpoint(dto.getApiEndpoint());
        vendor.setApiDocumentUrl(dto.getApiDocumentUrl());
        vendor.setCallbackUrl(dto.getCallbackUrl());
        vendor.setApiVersion(dto.getApiVersion());
    }

    /**
     * 创建审核记录并初始化流程实例
     */
    private void createAuditRecord(Integer vendorId, int round, AuditTypeEnum type, SubmitVendorDTO snapshot) {
        VendorAuditRecordPO record = new VendorAuditRecordPO();
        record.setVendorId(vendorId);
        record.setRound(round);
        record.setType(type);
        record.setData(snapshot);
        record.setAdminId(0);
        record.setAuditResult(AuditResultEnum.PENDING);
        vendorAuditRecordMapper.insert(record);

        // 获取所有启用的审核节点，按order排序
        LambdaQueryWrapper<AuditNodePO> nodeWrapper = Wrappers.lambdaQuery();
        nodeWrapper.eq(AuditNodePO::getIsActive, true);
        nodeWrapper.orderByAsc(AuditNodePO::getOrder);
        List<AuditNodePO> nodes = auditNodeMapper.selectList(nodeWrapper);

        // 为每个节点创建流程实例
        for (AuditNodePO node : nodes) {
            AuditInstancePO instance = new AuditInstancePO();
            instance.setVendorId(vendorId);
            instance.setAuditRecordId(record.getAuditRecordId());
            instance.setAuditNodeId(node.getAuditNodeId());
            auditInstanceMapper.insert(instance);
        }
    }
}

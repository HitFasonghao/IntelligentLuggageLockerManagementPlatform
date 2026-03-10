package org.example.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.audit.enums.VendorStatusEnum;
import org.example.audit.dto.SubmitVendorDTO;
import org.example.audit.mapper.*;
import org.example.audit.po.*;
import org.example.audit.service.VendorService;
import org.example.audit.statemachine.AuditEvent;
import org.example.audit.statemachine.AuditStateMachine;
import org.example.audit.vo.*;
import org.example.auth.common.PcUserInfo;
import org.example.auth.common.UserContext;
import org.example.auth.constants.HttpStatusConstants;
import org.example.auth.enums.PcUserIdentityEnum;
import org.example.auth.vo.HttpResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 厂商服务实现（厂商用户端）
 * <p>提交/重新提交等审核流程操作统一委托给 {@link AuditStateMachine} 处理
 *
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

    @Autowired
    private AuditStateMachine stateMachine;

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
     * 提交入驻申请（委托状态机处理流程流转）
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
            vendorMapper.updateById(vendor);

            // 委托状态机处理流程流转
            HttpResponseVO<String> result = stateMachine.fire(vendor.getVendorId(), AuditEvent.SUBMIT, dto);
            return HttpResponseVO.<Integer>builder()
                    .data(vendor.getVendorId())
                    .code(result.getCode())
                    .msg(result.getMsg())
                    .build();
        }

        // 直接新建并提交
        VendorPO vendor = new VendorPO();
        copyDtoToVendor(dto, vendor);
        vendor.setStatus(VendorStatusEnum.DRAFT);
        vendorMapper.insert(vendor);

        VendorUserRelationPO relation = new VendorUserRelationPO();
        relation.setVendorUserId(userInfo.getUserId());
        relation.setVendorId(vendor.getVendorId());
        relation.setIsMain(true);
        vendorUserRelationMapper.insert(relation);

        // 委托状态机处理流程流转
        HttpResponseVO<String> result = stateMachine.fire(vendor.getVendorId(), AuditEvent.SUBMIT, dto);
        return HttpResponseVO.<Integer>builder()
                .data(vendor.getVendorId())
                .code(result.getCode())
                .msg(result.getMsg())
                .build();
    }

    /**
     * 驳回后重新提交（委托状态机处理流程流转）
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

        if (!isVendorUser(userInfo.getUserId(), vendorId)) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("无权操作该厂商")
                    .build();
        }

        // 更新厂商信息
        copyDtoToVendor(dto, vendor);
        vendorMapper.updateById(vendor);

        // 委托状态机处理流程流转（状态校验由状态机完成）
        return stateMachine.fire(vendorId, AuditEvent.RESUBMIT, dto);
    }

    /**
     * 删除草稿
     */
    @Override
    @Transactional
    public HttpResponseVO<String> deleteDraft(Integer vendorId) {
        PcUserInfo userInfo = UserContext.get();

        VendorPO vendor = vendorMapper.selectById(vendorId);
        if (vendor == null) {
            return HttpResponseVO.<String>builder().code(HttpStatusConstants.ERROR).msg("厂商不存在").build();
        }
        if (vendor.getStatus() != VendorStatusEnum.DRAFT) {
            return HttpResponseVO.<String>builder().code(HttpStatusConstants.ERROR).msg("仅草稿状态可删除").build();
        }
        if (!isVendorUser(userInfo.getUserId(), vendorId)) {
            return HttpResponseVO.<String>builder().code(HttpStatusConstants.ERROR).msg("无权操作该厂商").build();
        }

        // 删除关联关系
        LambdaQueryWrapper<VendorUserRelationPO> relWrapper = Wrappers.lambdaQuery();
        relWrapper.eq(VendorUserRelationPO::getVendorId, vendorId);
        vendorUserRelationMapper.delete(relWrapper);

        // 删除厂商记录
        vendorMapper.deleteById(vendorId);

        return HttpResponseVO.<String>builder().code(HttpStatusConstants.SUCCESS).msg("草稿已删除").build();
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

    private boolean isVendorUser(Integer userId, Integer vendorId) {
        LambdaQueryWrapper<VendorUserRelationPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(VendorUserRelationPO::getVendorUserId, userId);
        wrapper.eq(VendorUserRelationPO::getVendorId, vendorId);
        return vendorUserRelationMapper.selectCount(wrapper) > 0;
    }

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
}

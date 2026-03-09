package org.example.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.audit.dto.FinalApprovalDTO;
import org.example.audit.dto.QualificationAuditDTO;
import org.example.audit.dto.TechTestAuditDTO;
import org.example.audit.enums.*;
import org.example.audit.mapper.*;
import org.example.audit.po.*;
import org.example.audit.service.AuditService;
import org.example.audit.vo.VendorAuditRecordVO;
import org.example.audit.vo.VendorListVO;
import org.example.audit.vo.VendorVO;
import org.example.auth.common.PcUserInfo;
import org.example.auth.common.UserContext;
import org.example.auth.constants.HttpStatusConstants;
import org.example.auth.vo.HttpResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 审核服务实现（平台管理员端）
 * @author fasonghao
 */
@Service
public class AuditServiceImpl implements AuditService {

    @Autowired
    private VendorMapper vendorMapper;

    @Autowired
    private VendorAuditRecordMapper vendorAuditRecordMapper;

    @Autowired
    private AuditInstanceMapper auditInstanceMapper;

    @Autowired
    private AuditNodeMapper auditNodeMapper;

    @Autowired
    private AuditMapStructMapper mapStructMapper;

    /**
     * 获取厂商审核列表
     */
    @Override
    public HttpResponseVO<List<VendorListVO>> getVendorList(VendorStatusEnum status) {
        LambdaQueryWrapper<VendorPO> wrapper = Wrappers.lambdaQuery();
        // 排除草稿状态
        wrapper.ne(VendorPO::getStatus, VendorStatusEnum.DRAFT);
        if (status != null) {
            wrapper.eq(VendorPO::getStatus, status);
        }
        wrapper.orderByDesc(VendorPO::getSubmittedTime);
        List<VendorPO> vendors = vendorMapper.selectList(wrapper);

        List<VendorListVO> voList = vendors.stream().map(v -> {
            VendorListVO vo = new VendorListVO();
            vo.setVendorId(v.getVendorId());
            vo.setCompanyName(v.getCompanyName());
            vo.setShortName(v.getShortName());
            vo.setContactPerson(v.getContactPerson());
            vo.setContactPhone(v.getContactPhone());
            vo.setStatus(v.getStatus());
            vo.setSubmittedTime(v.getSubmittedTime());
            vo.setCreatedTime(v.getCreatedTime());
            // 查询当前最大轮次
            LambdaQueryWrapper<VendorAuditRecordPO> recordWrapper = Wrappers.lambdaQuery();
            recordWrapper.eq(VendorAuditRecordPO::getVendorId, v.getVendorId());
            recordWrapper.orderByDesc(VendorAuditRecordPO::getRound);
            recordWrapper.last("LIMIT 1");
            VendorAuditRecordPO lastRecord = vendorAuditRecordMapper.selectOne(recordWrapper);
            vo.setCurrentRound(lastRecord != null ? lastRecord.getRound() : 0);
            return vo;
        }).collect(Collectors.toList());

        return HttpResponseVO.<List<VendorListVO>>builder()
                .data(voList)
                .code(HttpStatusConstants.SUCCESS)
                .msg("获取厂商列表成功")
                .build();
    }

    /**
     * 获取厂商详情
     */
    @Override
    public HttpResponseVO<VendorVO> getVendorDetail(Integer vendorId) {
        VendorPO vendor = vendorMapper.selectById(vendorId);
        if (vendor == null) {
            return HttpResponseVO.<VendorVO>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("厂商不存在")
                    .build();
        }
        return HttpResponseVO.<VendorVO>builder()
                .data(mapStructMapper.vendorPoToVo(vendor))
                .code(HttpStatusConstants.SUCCESS)
                .msg("获取厂商详情成功")
                .build();
    }

    /**
     * 获取厂商的审核记录列表
     */
    @Override
    public HttpResponseVO<List<VendorAuditRecordVO>> getAuditRecords(Integer vendorId) {
        LambdaQueryWrapper<VendorAuditRecordPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(VendorAuditRecordPO::getVendorId, vendorId);
        wrapper.orderByDesc(VendorAuditRecordPO::getRound);
        List<VendorAuditRecordPO> records = vendorAuditRecordMapper.selectList(wrapper);

        List<VendorAuditRecordVO> voList = records.stream()
                .map(mapStructMapper::auditRecordPoToVo)
                .collect(Collectors.toList());

        return HttpResponseVO.<List<VendorAuditRecordVO>>builder()
                .data(voList)
                .code(HttpStatusConstants.SUCCESS)
                .msg("获取审核记录成功")
                .build();
    }

    /**
     * 资质审核
     */
    @Override
    @Transactional
    public HttpResponseVO<String> qualificationAudit(Integer vendorId, QualificationAuditDTO dto) {
        PcUserInfo userInfo = UserContext.get();

        VendorPO vendor = vendorMapper.selectById(vendorId);
        if (vendor == null) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("厂商不存在")
                    .build();
        }
        if (vendor.getStatus() != VendorStatusEnum.PENDING) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("当前状态不允许进行资质审核")
                    .build();
        }

        // 获取最新审核记录
        VendorAuditRecordPO record = getLatestRecord(vendorId);
        if (record == null) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("未找到审核记录")
                    .build();
        }

        // 更新审核记录
        record.setAdminId(userInfo.getUserId());
        record.setAuditResult(dto.getAuditResult());
        record.setAuditNotes(dto.getAuditNotes());
        vendorAuditRecordMapper.updateById(record);

        // 更新资质审核节点的实例
        completeNodeInstance(record.getAuditRecordId(), AuditNodeTypeEnum.QUALIFICATION);

        if (dto.getAuditResult() == AuditResultEnum.PASS) {
            // 通过 → 进入技术测试阶段
            vendor.setStatus(VendorStatusEnum.TESTING);
            vendor.setReviewedTime(LocalDateTime.now());
            vendorMapper.updateById(vendor);

            // 启动下一个节点实例（API测试）
            startNodeInstance(record.getAuditRecordId(), AuditNodeTypeEnum.API_TEST);

            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.SUCCESS)
                    .msg("资质审核通过，已进入技术测试阶段")
                    .build();
        } else {
            // 不通过 → 驳回
            vendor.setStatus(VendorStatusEnum.REJECTED);
            vendor.setReviewedTime(LocalDateTime.now());
            vendorMapper.updateById(vendor);

            record.setCompletedTime(LocalDateTime.now());
            vendorAuditRecordMapper.updateById(record);

            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.SUCCESS)
                    .msg("资质审核未通过，已驳回")
                    .build();
        }
    }

    /**
     * 技术测试审核
     */
    @Override
    @Transactional
    public HttpResponseVO<String> techTestAudit(Integer vendorId, TechTestAuditDTO dto) {
        PcUserInfo userInfo = UserContext.get();

        VendorPO vendor = vendorMapper.selectById(vendorId);
        if (vendor == null) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("厂商不存在")
                    .build();
        }
        if (vendor.getStatus() != VendorStatusEnum.TESTING) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("当前状态不允许进行技术测试审核")
                    .build();
        }

        VendorAuditRecordPO record = getLatestRecord(vendorId);
        if (record == null) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("未找到审核记录")
                    .build();
        }

        // 更新测试相关字段
        record.setTestResult(dto.getTestResult());
        record.setTestNotes(dto.getTestNotes());
        record.setApiValidationResult(dto.getApiValidationResult());
        record.setPerformanceResult(dto.getPerformanceResult());

        if (dto.getTestResult() == TestResultEnum.TESTING) {
            // 标记测试进行中
            record.setTestStartedTime(LocalDateTime.now());
            vendorAuditRecordMapper.updateById(record);

            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.SUCCESS)
                    .msg("已开始技术测试")
                    .build();
        }

        record.setTestCompletedTime(LocalDateTime.now());
        vendorAuditRecordMapper.updateById(record);

        // 完成API测试和性能测试节点
        completeNodeInstance(record.getAuditRecordId(), AuditNodeTypeEnum.API_TEST);
        completeNodeInstance(record.getAuditRecordId(), AuditNodeTypeEnum.PERFORMANCE);

        if (dto.getTestResult() == TestResultEnum.PASSED) {
            // 测试通过，启动人工审核节点（最终审批）
            startNodeInstance(record.getAuditRecordId(), AuditNodeTypeEnum.MANUAL_REVIEW);

            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.SUCCESS)
                    .msg("技术测试通过，等待最终审批")
                    .build();
        } else if (dto.getTestResult() == TestResultEnum.FAILED) {
            // 测试失败 → 驳回
            vendor.setStatus(VendorStatusEnum.REJECTED);
            vendorMapper.updateById(vendor);

            record.setCompletedTime(LocalDateTime.now());
            vendorAuditRecordMapper.updateById(record);

            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.SUCCESS)
                    .msg("技术测试未通过，已驳回")
                    .build();
        }

        return HttpResponseVO.<String>builder()
                .code(HttpStatusConstants.SUCCESS)
                .msg("测试状态已更新")
                .build();
    }

    /**
     * 最终审批
     */
    @Override
    @Transactional
    public HttpResponseVO<String> finalApproval(Integer vendorId, FinalApprovalDTO dto) {
        PcUserInfo userInfo = UserContext.get();

        VendorPO vendor = vendorMapper.selectById(vendorId);
        if (vendor == null) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("厂商不存在")
                    .build();
        }
        if (vendor.getStatus() != VendorStatusEnum.TESTING) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("当前状态不允许进行最终审批")
                    .build();
        }

        VendorAuditRecordPO record = getLatestRecord(vendorId);
        if (record == null) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("未找到审核记录")
                    .build();
        }

        // 完成人工审核节点
        completeNodeInstance(record.getAuditRecordId(), AuditNodeTypeEnum.MANUAL_REVIEW);

        if (dto.getApproved()) {
            // 批准
            vendor.setStatus(VendorStatusEnum.APPROVED);
            vendor.setApprovedTime(LocalDateTime.now());
            vendor.setAdminId(userInfo.getUserId());
            vendor.setEffectiveFrom(dto.getEffectiveFrom());
            vendor.setEffectiveTo(dto.getEffectiveTo());
            // 自动生成厂商编码
            vendor.setVendorCode(generateVendorCode());
            vendorMapper.updateById(vendor);

            record.setCompletedTime(LocalDateTime.now());
            record.setAuditNotes(record.getAuditNotes() != null
                    ? record.getAuditNotes() + "；最终审批意见：" + dto.getNotes()
                    : "最终审批意见：" + dto.getNotes());
            vendorAuditRecordMapper.updateById(record);

            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.SUCCESS)
                    .msg("最终审批通过，厂商入驻成功")
                    .build();
        } else {
            // 驳回
            vendor.setStatus(VendorStatusEnum.REJECTED);
            vendor.setAdminId(userInfo.getUserId());
            vendorMapper.updateById(vendor);

            record.setAuditResult(AuditResultEnum.FAIL);
            record.setCompletedTime(LocalDateTime.now());
            record.setAuditNotes(record.getAuditNotes() != null
                    ? record.getAuditNotes() + "；最终审批意见：" + dto.getNotes()
                    : "最终审批意见：" + dto.getNotes());
            vendorAuditRecordMapper.updateById(record);

            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.SUCCESS)
                    .msg("最终审批未通过，已驳回")
                    .build();
        }
    }

    // ========== 私有方法 ==========

    private VendorAuditRecordPO getLatestRecord(Integer vendorId) {
        LambdaQueryWrapper<VendorAuditRecordPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(VendorAuditRecordPO::getVendorId, vendorId);
        wrapper.orderByDesc(VendorAuditRecordPO::getRound);
        wrapper.last("LIMIT 1");
        return vendorAuditRecordMapper.selectOne(wrapper);
    }

    /**
     * 完成指定类型的流程节点实例
     */
    private void completeNodeInstance(Integer auditRecordId, AuditNodeTypeEnum nodeType) {
        // 找到对应节点
        LambdaQueryWrapper<AuditNodePO> nodeWrapper = Wrappers.lambdaQuery();
        nodeWrapper.eq(AuditNodePO::getType, nodeType);
        nodeWrapper.eq(AuditNodePO::getIsActive, true);
        AuditNodePO node = auditNodeMapper.selectOne(nodeWrapper);
        if (node == null) return;

        LambdaUpdateWrapper<AuditInstancePO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AuditInstancePO::getAuditRecordId, auditRecordId);
        wrapper.eq(AuditInstancePO::getAuditNodeId, node.getAuditNodeId());
        wrapper.set(AuditInstancePO::getCompletedTime, LocalDateTime.now());
        auditInstanceMapper.update(null, wrapper);
    }

    /**
     * 启动指定类型的流程节点实例
     */
    private void startNodeInstance(Integer auditRecordId, AuditNodeTypeEnum nodeType) {
        LambdaQueryWrapper<AuditNodePO> nodeWrapper = Wrappers.lambdaQuery();
        nodeWrapper.eq(AuditNodePO::getType, nodeType);
        nodeWrapper.eq(AuditNodePO::getIsActive, true);
        AuditNodePO node = auditNodeMapper.selectOne(nodeWrapper);
        if (node == null) return;

        LambdaUpdateWrapper<AuditInstancePO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AuditInstancePO::getAuditRecordId, auditRecordId);
        wrapper.eq(AuditInstancePO::getAuditNodeId, node.getAuditNodeId());
        wrapper.set(AuditInstancePO::getStartedTime, LocalDateTime.now());
        auditInstanceMapper.update(null, wrapper);
    }

    /**
     * 生成厂商编码（V + 年月日 + 4位随机）
     */
    private String generateVendorCode() {
        String datePart = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "V" + datePart + randomPart;
    }
}

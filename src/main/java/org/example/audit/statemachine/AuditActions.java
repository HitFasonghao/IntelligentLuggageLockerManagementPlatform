package org.example.audit.statemachine;

import org.example.audit.dto.*;
import org.example.audit.enums.*;
import org.example.audit.mapper.VendorAuditRecordMapper;
import org.example.audit.po.VendorAuditRecordPO;
import org.example.audit.po.VendorPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 所有审核动作的实现，每个方法对应一个 AuditEvent
 * @author fasonghao
 */
@Component
public class AuditActions {

    @Autowired
    private AuditNodeHelper nodeHelper;

    @Autowired
    private VendorAuditRecordMapper vendorAuditRecordMapper;

    // ==================== 厂商端动作 ====================

    /**
     * 提交入驻申请
     */
    public String onSubmit(AuditContext ctx) {
        VendorPO vendor = ctx.getVendor();
        SubmitVendorDTO dto = (SubmitVendorDTO) ctx.getDto();

        vendor.setSubmittedTime(LocalDateTime.now());

        // 创建第1轮审核记录及第一个节点任务
        nodeHelper.createRecordWithFirstTask(
                vendor.getVendorId(), ctx.getUserInfo().getUserId(), 1, AuditTypeEnum.INITIAL, dto);

        return "入驻申请提交成功";
    }

    /**
     * 驳回后重新提交
     */
    public String onResubmit(AuditContext ctx) {
        VendorPO vendor = ctx.getVendor();
        SubmitVendorDTO dto = (SubmitVendorDTO) ctx.getDto();

        vendor.setSubmittedTime(LocalDateTime.now());

        // 计算下一轮次
        VendorAuditRecordPO lastRecord = nodeHelper.getLatestRecord(vendor.getVendorId());
        int nextRound = (lastRecord != null) ? lastRecord.getRound() + 1 : 1;

        // 创建新一轮审核记录及第一个节点任务
        nodeHelper.createRecordWithFirstTask(
                vendor.getVendorId(), ctx.getUserInfo().getUserId(), nextRound, AuditTypeEnum.RENEWAL, dto);

        return "重新提交成功";
    }

    // ==================== 平台管理员端动作 ====================

    /**
     * 资质审核通过
     */
    public String onQualificationPass(AuditContext ctx) {
        VendorPO vendor = ctx.getVendor();
        VendorAuditRecordPO record = ctx.getLatestRecord();
        QualificationAuditDTO dto = (QualificationAuditDTO) ctx.getDto();

        record.setAdminId(ctx.getUserInfo().getUserId());
        record.setResult(AuditRecordResultEnum.PENDING_FUNCTIONAL_TEST);
        vendorAuditRecordMapper.updateById(record);

        nodeHelper.completeNode(record.getAuditRecordId(), AuditNodeTypeEnum.QUALIFICATION, true, dto.getNotes());

        // 创建下一个节点任务并自动分配同一管理员
        nodeHelper.createNextTask(vendor.getVendorId(), record.getAuditRecordId(), AuditNodeTypeEnum.FUNCTIONAL_TEST);
        nodeHelper.autoAssignTask(record.getAuditRecordId(), AuditNodeTypeEnum.FUNCTIONAL_TEST, ctx.getUserInfo().getUserId());

        vendor.setReviewedTime(LocalDateTime.now());

        return "资质审核通过，已进入功能测试阶段";
    }

    /**
     * 资质审核不通过
     */
    public String onQualificationFail(AuditContext ctx) {
        VendorPO vendor = ctx.getVendor();
        VendorAuditRecordPO record = ctx.getLatestRecord();
        QualificationAuditDTO dto = (QualificationAuditDTO) ctx.getDto();

        record.setAdminId(ctx.getUserInfo().getUserId());
        record.setResult(AuditRecordResultEnum.QUALIFICATION_FAILED);
        record.setCompletedTime(LocalDateTime.now());
        vendorAuditRecordMapper.updateById(record);

        nodeHelper.completeNode(record.getAuditRecordId(), AuditNodeTypeEnum.QUALIFICATION, false, dto.getNotes());

        vendor.setReviewedTime(LocalDateTime.now());

        return "资质审核未通过，已驳回";
    }

    /**
     * 功能测试通过
     */
    public String onFunctionalTestPass(AuditContext ctx) {
        VendorPO vendor = ctx.getVendor();
        VendorAuditRecordPO record = ctx.getLatestRecord();
        FunctionalTestDTO dto = (FunctionalTestDTO) ctx.getDto();

        record.setResult(AuditRecordResultEnum.PENDING_PERFORMANCE_TEST);
        vendorAuditRecordMapper.updateById(record);

        nodeHelper.completeNode(record.getAuditRecordId(), AuditNodeTypeEnum.FUNCTIONAL_TEST, true, dto.getNotes());

        // 创建下一个节点任务并自动分配同一管理员
        nodeHelper.createNextTask(vendor.getVendorId(), record.getAuditRecordId(), AuditNodeTypeEnum.PERFORMANCE);
        nodeHelper.autoAssignTask(record.getAuditRecordId(), AuditNodeTypeEnum.PERFORMANCE, ctx.getUserInfo().getUserId());

        return "功能测试通过，已进入性能测试阶段";
    }

    /**
     * 功能测试失败
     */
    public String onFunctionalTestFail(AuditContext ctx) {
        VendorAuditRecordPO record = ctx.getLatestRecord();
        FunctionalTestDTO dto = (FunctionalTestDTO) ctx.getDto();

        record.setResult(AuditRecordResultEnum.FUNCTIONAL_TEST_FAILED);
        record.setCompletedTime(LocalDateTime.now());
        vendorAuditRecordMapper.updateById(record);

        nodeHelper.completeNode(record.getAuditRecordId(), AuditNodeTypeEnum.FUNCTIONAL_TEST, false, dto.getNotes());

        return "功能测试未通过，已驳回";
    }

    /**
     * 性能测试通过
     */
    public String onPerformancePass(AuditContext ctx) {
        VendorPO vendor = ctx.getVendor();
        VendorAuditRecordPO record = ctx.getLatestRecord();
        PerformanceTestDTO dto = (PerformanceTestDTO) ctx.getDto();

        record.setResult(AuditRecordResultEnum.PENDING_FINAL_APPROVAL);
        vendorAuditRecordMapper.updateById(record);

        nodeHelper.completeNode(record.getAuditRecordId(), AuditNodeTypeEnum.PERFORMANCE, true, dto.getNotes());

        // 创建下一个节点任务并自动分配同一管理员
        nodeHelper.createNextTask(vendor.getVendorId(), record.getAuditRecordId(), AuditNodeTypeEnum.MANUAL_REVIEW);
        nodeHelper.autoAssignTask(record.getAuditRecordId(), AuditNodeTypeEnum.MANUAL_REVIEW, ctx.getUserInfo().getUserId());

        return "性能测试通过，等待最终审批";
    }

    /**
     * 性能测试失败
     */
    public String onPerformanceFail(AuditContext ctx) {
        VendorAuditRecordPO record = ctx.getLatestRecord();
        PerformanceTestDTO dto = (PerformanceTestDTO) ctx.getDto();

        record.setResult(AuditRecordResultEnum.PERFORMANCE_TEST_FAILED);
        record.setCompletedTime(LocalDateTime.now());
        vendorAuditRecordMapper.updateById(record);

        nodeHelper.completeNode(record.getAuditRecordId(), AuditNodeTypeEnum.PERFORMANCE, false, dto.getNotes());

        return "性能测试未通过，已驳回";
    }

    /**
     * 最终审批通过
     */
    public String onFinalApprove(AuditContext ctx) {
        VendorPO vendor = ctx.getVendor();
        VendorAuditRecordPO record = ctx.getLatestRecord();
        FinalApprovalDTO dto = (FinalApprovalDTO) ctx.getDto();

        nodeHelper.completeNode(record.getAuditRecordId(), AuditNodeTypeEnum.MANUAL_REVIEW, true, dto.getNotes());

        vendor.setApprovedTime(LocalDateTime.now());
        vendor.setAdminId(ctx.getUserInfo().getUserId());
        vendor.setEffectiveFrom(dto.getEffectiveFrom());
        vendor.setEffectiveTo(dto.getEffectiveTo());
        vendor.setPlatformAccessToken(generatePlatformAccessToken());

        record.setResult(AuditRecordResultEnum.APPROVED);
        record.setCompletedTime(LocalDateTime.now());
        vendorAuditRecordMapper.updateById(record);

        return "最终审批通过，厂商入驻成功";
    }

    /**
     * 最终审批驳回
     */
    public String onFinalReject(AuditContext ctx) {
        VendorPO vendor = ctx.getVendor();
        VendorAuditRecordPO record = ctx.getLatestRecord();
        FinalApprovalDTO dto = (FinalApprovalDTO) ctx.getDto();

        nodeHelper.completeNode(record.getAuditRecordId(), AuditNodeTypeEnum.MANUAL_REVIEW, false, dto.getNotes());

        vendor.setAdminId(ctx.getUserInfo().getUserId());

        record.setResult(AuditRecordResultEnum.FINAL_APPROVAL_REJECTED);
        record.setCompletedTime(LocalDateTime.now());
        vendorAuditRecordMapper.updateById(record);

        return "最终审批未通过，已驳回";
    }

    // ==================== 厂商管理动作 ====================

    public String onSuspend(AuditContext ctx) {
        VendorOperationDTO dto = (VendorOperationDTO) ctx.getDto();
        ctx.getVendor().setAdminId(ctx.getUserInfo().getUserId());
        return "厂商已暂停合作" + (dto.getNotes() != null ? "，原因：" + dto.getNotes() : "");
    }

    public String onRestore(AuditContext ctx) {
        ctx.getVendor().setAdminId(ctx.getUserInfo().getUserId());
        return "厂商已恢复合作";
    }

    public String onBan(AuditContext ctx) {
        VendorOperationDTO dto = (VendorOperationDTO) ctx.getDto();
        ctx.getVendor().setAdminId(ctx.getUserInfo().getUserId());
        return "厂商已被封禁" + (dto.getNotes() != null ? "，原因：" + dto.getNotes() : "");
    }

    // ==================== 工具方法 ====================

    private String generatePlatformAccessToken() {
        return "PAT-" + UUID.randomUUID().toString().replace("-", "");
    }
}

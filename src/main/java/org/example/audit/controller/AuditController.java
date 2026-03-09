package org.example.audit.controller;

import jakarta.validation.Valid;
import org.example.audit.dto.FinalApprovalDTO;
import org.example.audit.dto.QualificationAuditDTO;
import org.example.audit.dto.TechTestAuditDTO;
import org.example.audit.enums.VendorStatusEnum;
import org.example.audit.service.AuditService;
import org.example.audit.vo.VendorAuditRecordVO;
import org.example.audit.vo.VendorListVO;
import org.example.audit.vo.VendorVO;
import org.example.auth.vo.HttpResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审核管理接口（平台管理员使用）
 * @author fasonghao
 */
@RestController
@RequestMapping("/audit")
public class AuditController {

    @Autowired
    private AuditService auditService;

    /**
     * 获取厂商审核列表
     */
    @GetMapping("/vendors")
    public HttpResponseVO<List<VendorListVO>> getVendorList(
            @RequestParam(required = false) VendorStatusEnum status) {
        return auditService.getVendorList(status);
    }

    /**
     * 获取厂商详情
     */
    @GetMapping("/vendors/{vendorId}")
    public HttpResponseVO<VendorVO> getVendorDetail(@PathVariable Integer vendorId) {
        return auditService.getVendorDetail(vendorId);
    }

    /**
     * 获取厂商的审核记录列表
     */
    @GetMapping("/vendors/{vendorId}/records")
    public HttpResponseVO<List<VendorAuditRecordVO>> getAuditRecords(@PathVariable Integer vendorId) {
        return auditService.getAuditRecords(vendorId);
    }

    /**
     * 资质审核
     */
    @PostMapping("/vendors/{vendorId}/qualification")
    public HttpResponseVO<String> qualificationAudit(@PathVariable Integer vendorId,
                                                     @RequestBody @Valid QualificationAuditDTO dto) {
        return auditService.qualificationAudit(vendorId, dto);
    }

    /**
     * 技术测试审核
     */
    @PostMapping("/vendors/{vendorId}/techTest")
    public HttpResponseVO<String> techTestAudit(@PathVariable Integer vendorId,
                                                @RequestBody @Valid TechTestAuditDTO dto) {
        return auditService.techTestAudit(vendorId, dto);
    }

    /**
     * 最终审批
     */
    @PostMapping("/vendors/{vendorId}/finalApproval")
    public HttpResponseVO<String> finalApproval(@PathVariable Integer vendorId,
                                                @RequestBody @Valid FinalApprovalDTO dto) {
        return auditService.finalApproval(vendorId, dto);
    }
}

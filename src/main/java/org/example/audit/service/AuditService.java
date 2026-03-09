package org.example.audit.service;

import org.example.audit.dto.FinalApprovalDTO;
import org.example.audit.dto.QualificationAuditDTO;
import org.example.audit.dto.TechTestAuditDTO;
import org.example.audit.enums.VendorStatusEnum;
import org.example.audit.vo.VendorAuditRecordVO;
import org.example.audit.vo.VendorListVO;
import org.example.audit.vo.VendorVO;
import org.example.auth.vo.HttpResponseVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 审核服务接口（平台管理员端）
 * @author fasonghao
 */
@Service
public interface AuditService {

    /** 获取厂商审核列表（支持按状态筛选） */
    HttpResponseVO<List<VendorListVO>> getVendorList(VendorStatusEnum status);

    /** 获取厂商详情（含资质信息） */
    HttpResponseVO<VendorVO> getVendorDetail(Integer vendorId);

    /** 获取厂商的审核记录列表 */
    HttpResponseVO<List<VendorAuditRecordVO>> getAuditRecords(Integer vendorId);

    /** 资质审核 */
    HttpResponseVO<String> qualificationAudit(Integer vendorId, QualificationAuditDTO dto);

    /** 技术测试审核 */
    HttpResponseVO<String> techTestAudit(Integer vendorId, TechTestAuditDTO dto);

    /** 最终审批 */
    HttpResponseVO<String> finalApproval(Integer vendorId, FinalApprovalDTO dto);
}

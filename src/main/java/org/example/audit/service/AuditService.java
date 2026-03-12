package org.example.audit.service;

import org.example.audit.dto.AuditRecordQueryDTO;
import org.example.audit.dto.FinalApprovalDTO;
import org.example.audit.dto.PerformanceTestDTO;
import org.example.audit.dto.QualificationAuditDTO;
import org.example.audit.dto.FunctionalTestDTO;
import org.example.audit.dto.VendorAuditQueryDTO;
import org.example.audit.vo.AuditTaskVO;
import org.example.audit.vo.VendorAuditRecordVO;
import org.example.audit.vo.VendorListVO;
import org.example.audit.vo.VendorVO;
import org.example.auth.vo.HttpResponseVO;

import java.util.List;
import java.util.Map;

/**
 * 审核服务接口（平台管理员端）
 * @author fasonghao
 */
public interface AuditService {

    /** 获取厂商审核列表（支持条件查询+分页） */
    HttpResponseVO<Map<String, Object>> getVendorList(VendorAuditQueryDTO queryDTO);

    /** 获取厂商详情（含资质信息），传 recordId 时从审核记录快照中读取 */
    HttpResponseVO<VendorVO> getVendorDetail(Integer vendorId, Integer recordId);

    /** 获取厂商的审核记录列表 */
    HttpResponseVO<List<VendorAuditRecordVO>> getAuditRecords(Integer vendorId);

    /** 资质审核 */
    HttpResponseVO<String> qualificationAudit(Integer vendorId, QualificationAuditDTO dto);

    /** 功能测试审核 */
    HttpResponseVO<String> functionalTestAudit(Integer vendorId, FunctionalTestDTO dto);

    /** 性能测试审核 */
    HttpResponseVO<String> performanceTest(Integer vendorId, PerformanceTestDTO dto);

    /** 最终审批 */
    HttpResponseVO<String> finalApproval(Integer vendorId, FinalApprovalDTO dto);

    /** 获取当前管理员的审核完成记录（审核记录页使用） */
    HttpResponseVO<Map<String, Object>> getMyAuditRecords(AuditRecordQueryDTO queryDTO);

}

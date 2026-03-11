package org.example.audit.service;

import org.example.audit.dto.SubmitVendorDTO;
import org.example.audit.vo.AuditProgressVO;
import org.example.audit.vo.VendorListVO;
import org.example.audit.vo.VendorVO;
import org.example.auth.vo.HttpResponseVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 厂商服务接口（厂商用户端）
 * @author fasonghao
 */
@Service
public interface VendorService {

    /** 保存草稿 */
    HttpResponseVO<Integer> saveDraft(SubmitVendorDTO dto);

    /** 提交入驻申请 */
    HttpResponseVO<Integer> submitApplication(SubmitVendorDTO dto);

    /** 驳回后重新提交 */
    HttpResponseVO<String> resubmit(Integer vendorId, SubmitVendorDTO dto);

    /** 删除草稿 */
    HttpResponseVO<String> deleteDraft(Integer vendorId);

    /** 获取当前厂商用户关联的所有厂商列表 */
    HttpResponseVO<List<VendorVO>> getMyVendors();

    /** 获取当前厂商用户关联的厂商信息（单个） */
    HttpResponseVO<VendorVO> getMyVendorInfo();

    /** 获取审核进度 */
    HttpResponseVO<AuditProgressVO> getAuditProgress(Integer vendorId);

    /** 获取当前厂商用户关联厂商的审核记录列表 */
    HttpResponseVO<List<VendorListVO>> getMyAuditRecords();
}

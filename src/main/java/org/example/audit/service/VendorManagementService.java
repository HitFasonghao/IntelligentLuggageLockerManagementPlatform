package org.example.audit.service;

import org.example.audit.dto.ApprovedVendorQueryDTO;
import org.example.audit.dto.VendorOperationDTO;
import org.example.audit.vo.VendorListVO;
import org.example.audit.vo.VendorVO;
import org.example.auth.vo.HttpResponseVO;

import java.util.List;
import java.util.Map;

/**
 * 厂商管理服务接口（已入驻厂商的日常管理）
 * @author fasonghao
 */
public interface VendorManagementService {

    /** 获取正常厂商列表（approved） */
    HttpResponseVO<Map<String, Object>> getApprovedVendors(ApprovedVendorQueryDTO queryDTO);

    /** 获取异常厂商列表（suspended / banned） */
    HttpResponseVO<List<VendorListVO>> getAbnormalVendors();

    /** 获取厂商详情 */
    HttpResponseVO<VendorVO> getVendorDetail(Integer vendorId);

    /** 暂停合作 */
    HttpResponseVO<String> suspendVendor(Integer vendorId, VendorOperationDTO dto);

    /** 恢复合作 */
    HttpResponseVO<String> restoreVendor(Integer vendorId, VendorOperationDTO dto);

    /** 封禁 */
    HttpResponseVO<String> banVendor(Integer vendorId, VendorOperationDTO dto);
}

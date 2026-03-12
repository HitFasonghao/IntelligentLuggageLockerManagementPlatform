package org.example.audit.controller;

import org.example.audit.dto.ApprovedVendorQueryDTO;
import org.example.audit.dto.VendorOperationDTO;
import org.example.audit.service.VendorManagementService;
import org.example.audit.vo.VendorListVO;
import org.example.audit.vo.VendorVO;
import org.example.auth.vo.HttpResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 厂商管理接口（已入驻厂商的日常管理，平台管理员使用）
 * @author fasonghao
 */
@RestController
@RequestMapping("/vendor-mgmt")
public class VendorManagementController {

    @Autowired
    private VendorManagementService vendorManagementService;

    /**
     * 获取正常厂商列表（approved）
     */
    @GetMapping("/approved")
    public HttpResponseVO<Map<String, Object>> getApprovedVendors(ApprovedVendorQueryDTO queryDTO) {
        return vendorManagementService.getApprovedVendors(queryDTO);
    }

    /**
     * 获取异常厂商列表（suspended / banned）
     */
    @GetMapping("/abnormal")
    public HttpResponseVO<List<VendorListVO>> getAbnormalVendors() {
        return vendorManagementService.getAbnormalVendors();
    }

    /**
     * 获取厂商详情
     */
    @GetMapping("/{vendorId}")
    public HttpResponseVO<VendorVO> getVendorDetail(@PathVariable Integer vendorId) {
        return vendorManagementService.getVendorDetail(vendorId);
    }

    /**
     * 暂停合作
     */
    @PostMapping("/{vendorId}/suspend")
    public HttpResponseVO<String> suspendVendor(@PathVariable Integer vendorId,
                                                @RequestBody VendorOperationDTO dto) {
        return vendorManagementService.suspendVendor(vendorId, dto);
    }

    /**
     * 恢复合作
     */
    @PostMapping("/{vendorId}/restore")
    public HttpResponseVO<String> restoreVendor(@PathVariable Integer vendorId,
                                                @RequestBody VendorOperationDTO dto) {
        return vendorManagementService.restoreVendor(vendorId, dto);
    }

    /**
     * 封禁
     */
    @PostMapping("/{vendorId}/ban")
    public HttpResponseVO<String> banVendor(@PathVariable Integer vendorId,
                                            @RequestBody VendorOperationDTO dto) {
        return vendorManagementService.banVendor(vendorId, dto);
    }
}

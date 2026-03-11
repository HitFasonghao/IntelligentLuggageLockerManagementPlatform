package org.example.audit.controller;

import jakarta.validation.Valid;
import org.example.audit.dto.SubmitVendorDTO;
import org.example.audit.service.FileStorageService;
import org.example.audit.service.VendorService;
import org.example.audit.service.VendorUserRelationService;
import org.example.audit.vo.AuditProgressVO;
import org.example.audit.vo.VendorListVO;
import org.example.audit.vo.VendorUserRelationVO;
import org.example.audit.vo.VendorVO;
import org.example.auth.constants.HttpStatusConstants;
import org.example.auth.vo.HttpResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 厂商端接口（厂商用户使用）
 * @author fasonghao
 */
@RestController
@RequestMapping("/vendor")
public class VendorController {

    @Autowired
    private VendorService vendorService;

    @Autowired
    private VendorUserRelationService vendorUserRelationService;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 保存草稿
     */
    @PostMapping("/draft")
    public HttpResponseVO<Integer> saveDraft(@RequestBody @Valid SubmitVendorDTO dto) {
        return vendorService.saveDraft(dto);
    }

    /**
     * 提交入驻申请
     */
    @PostMapping("/submit")
    public HttpResponseVO<Integer> submitApplication(@RequestBody @Valid SubmitVendorDTO dto) {
        return vendorService.submitApplication(dto);
    }

    /**
     * 删除草稿
     */
    @DeleteMapping("/draft/{vendorId}")
    public HttpResponseVO<String> deleteDraft(@PathVariable Integer vendorId) {
        return vendorService.deleteDraft(vendorId);
    }

    /**
     * 驳回后重新提交
     */
    @PostMapping("/{vendorId}/resubmit")
    public HttpResponseVO<String> resubmit(@PathVariable Integer vendorId,
                                           @RequestBody @Valid SubmitVendorDTO dto) {
        return vendorService.resubmit(vendorId, dto);
    }

    /**
     * 获取当前厂商用户关联的所有厂商列表
     */
    @GetMapping("/myVendors")
    public HttpResponseVO<List<VendorVO>> getMyVendors() {
        return vendorService.getMyVendors();
    }

    /**
     * 获取当前厂商用户关联厂商的审核记录列表
     */
    @GetMapping("/myAuditRecords")
    public HttpResponseVO<List<VendorListVO>> getMyAuditRecords() {
        return vendorService.getMyAuditRecords();
    }

    /**
     * 获取当前厂商用户关联的厂商信息（单个，兼容旧接口）
     */
    @GetMapping("/myVendorInfo")
    public HttpResponseVO<VendorVO> getMyVendorInfo() {
        return vendorService.getMyVendorInfo();
    }

    /**
     * 获取审核进度
     */
    @GetMapping("/{vendorId}/auditProgress")
    public HttpResponseVO<AuditProgressVO> getAuditProgress(@PathVariable Integer vendorId) {
        return vendorService.getAuditProgress(vendorId);
    }

    /**
     * 获取厂商下的用户列表
     */
    @GetMapping("/{vendorId}/users")
    public HttpResponseVO<List<VendorUserRelationVO>> getVendorUsers(@PathVariable Integer vendorId) {
        return vendorUserRelationService.getVendorUsers(vendorId);
    }

    /**
     * 添加厂商用户（通过用户名）
     */
    @PostMapping("/{vendorId}/users")
    public HttpResponseVO<String> addVendorUser(@PathVariable Integer vendorId,
                                                @RequestParam String username) {
        return vendorUserRelationService.addVendorUser(vendorId, username);
    }

    /**
     * 移除厂商用户
     */
    @DeleteMapping("/{vendorId}/users/{vendorUserId}")
    public HttpResponseVO<String> removeVendorUser(@PathVariable Integer vendorId,
                                                   @PathVariable Integer vendorUserId) {
        return vendorUserRelationService.removeVendorUser(vendorId, vendorUserId);
    }

    /**
     * 上传营业执照照片
     */
    @PostMapping("/upload/license")
    public HttpResponseVO<String> uploadLicense(@RequestParam("file") MultipartFile file,
                                                @RequestParam(value = "oldUrl", required = false) String oldUrl) {
        if (file.isEmpty()) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("文件不能为空")
                    .build();
        }
        String url = fileStorageService.uploadFile(file, "license");
        // 上传成功后删除旧文件
        if (oldUrl != null && !oldUrl.isBlank()) {
            fileStorageService.deleteFile(oldUrl);
        }
        return HttpResponseVO.<String>builder()
                .code(HttpStatusConstants.SUCCESS)
                .msg("上传成功")
                .data(url)
                .build();
    }
}

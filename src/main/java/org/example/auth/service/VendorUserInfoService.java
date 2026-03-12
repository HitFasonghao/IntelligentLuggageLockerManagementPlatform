package org.example.auth.service;

import jakarta.validation.Valid;
import org.example.auth.dto.UpdateVendorUserDTO;
import org.example.auth.vo.HttpResponseVO;
import org.example.auth.vo.VendorUserInfoVO;
import org.example.auth.vo.VendorSimpleVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author fasonghao
 */
public interface VendorUserInfoService {
    /**
     * 查询账号信息
     */
    HttpResponseVO<VendorUserInfoVO> queryVendorUserInfo();

    /**
     * 修改账号信息
     */
    HttpResponseVO<String> updateVendorUserInfo(@Valid UpdateVendorUserDTO updateVendorUserDTO);

    /**
     * 获取当前厂商用户的已审核通过厂商列表
     */
    HttpResponseVO<List<VendorSimpleVO>> getApprovedVendors();

    /**
     * 切换当前厂商
     */
    HttpResponseVO<String> switchVendor(Integer vendorId, String token);

    /**
     * 更换头像
     */
    HttpResponseVO<String> updateAvatar(MultipartFile file);

}

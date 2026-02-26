package org.example.auth.service;

import jakarta.validation.Valid;
import org.example.auth.dto.UpdateVendorUserDTO;
import org.example.auth.vo.HttpResponseVO;
import org.example.auth.vo.VendorUserInfoVO;

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

}

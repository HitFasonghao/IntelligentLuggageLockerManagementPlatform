package org.example.service;

import jakarta.validation.Valid;
import org.example.dto.CountIsExistDTO;
import org.example.dto.UpdateVendorUserDTO;
import org.example.vo.HttpResponseVO;
import org.example.vo.VendorUserInfoVO;

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

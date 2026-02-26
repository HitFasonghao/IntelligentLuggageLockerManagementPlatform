package org.example.auth.controller;

import jakarta.validation.Valid;
import org.example.auth.dto.UpdateVendorUserDTO;
import org.example.auth.service.VendorUserInfoService;
import org.example.auth.vo.HttpResponseVO;
import org.example.auth.vo.VendorUserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 厂商用户信息管理模块
 * @author fasonghao
 */
@RestController
@RequestMapping("/vendorUser")
public class VendorUserInfoController {

    @Autowired
    private VendorUserInfoService vendorUserInfoService;

    /**
     * 查询账号信息
     */
    @GetMapping("/queryVendorUserInfo")
    public HttpResponseVO<VendorUserInfoVO> queryVendorUserInfo(){
        return vendorUserInfoService.queryVendorUserInfo();
    }

    /**
     * 修改账号信息
     */
    @PutMapping("/updateInfo")
    public HttpResponseVO<String> updateVendorUserInfo(@RequestBody @Valid UpdateVendorUserDTO updateVendorUserDTO){
        return vendorUserInfoService.updateVendorUserInfo(updateVendorUserDTO);
    }
}

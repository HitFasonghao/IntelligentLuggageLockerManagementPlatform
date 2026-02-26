package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.UpdateVendorUserDTO;
import org.example.service.VendorUserInfoService;
import org.example.vo.HttpResponseVO;
import org.example.vo.VendorUserInfoVO;
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

    /**
     * 修改账号密码
     */
    @PutMapping("/updatePassword")
    public HttpResponseVO<String> updatePassword(){
        return null;
    }

    /**
     * 确认原手机号的验证码
     */
    @PostMapping("/confirmOldPhone")
    public HttpResponseVO<String> confirmOldPhone(){
        return null;
    }

    /**
     * 更换绑定的手机号
     */
    @PutMapping("/updatePhone")
    public HttpResponseVO<String> updatePhone(){
        return null;
    }
}

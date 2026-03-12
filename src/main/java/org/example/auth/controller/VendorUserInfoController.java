package org.example.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.auth.dto.UpdateVendorUserDTO;
import org.example.auth.service.VendorUserInfoService;
import org.example.auth.vo.HttpResponseVO;
import org.example.auth.vo.VendorSimpleVO;
import org.example.auth.vo.VendorUserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
     * 获取当前厂商用户的已审核通过厂商列表
     */
    @GetMapping("/approvedVendors")
    public HttpResponseVO<List<VendorSimpleVO>> getApprovedVendors(){
        return vendorUserInfoService.getApprovedVendors();
    }

    /**
     * 切换当前厂商
     */
    @PutMapping("/switchVendor/{vendorId}")
    public HttpResponseVO<String> switchVendor(@PathVariable Integer vendorId, HttpServletRequest request){
        String token = request.getHeader("Authorization");
        return vendorUserInfoService.switchVendor(vendorId, token);
    }

    /**
     * 更换头像
     */
    @PostMapping("/updateAvatar")
    public HttpResponseVO<String> updateAvatar(@RequestParam("file") MultipartFile file){
        return vendorUserInfoService.updateAvatar(file);
    }
}

package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.UpdateAdminDTO;
import org.example.dto.UpdateVendorUserDTO;
import org.example.service.AdminInfoService;
import org.example.vo.AdminInfoVO;
import org.example.vo.HttpResponseVO;
import org.example.vo.VendorUserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 平台管理员信息管理模块
 * @author fasonghao
 */
@RestController
@RequestMapping("/admin")
public class AdminInfoController {

    @Autowired
    private AdminInfoService adminInfoService;

    /**
     * 查询账号信息
     */
    @GetMapping("/queryVendorUserInfo")
    public HttpResponseVO<AdminInfoVO> queryAdminInfo(){
        return adminInfoService.queryAdminInfo();
    }

    /**
     * 修改账号信息
     */
    @PutMapping("/updateInfo")
    public HttpResponseVO<String> updateAdminInfo(@RequestBody @Valid UpdateAdminDTO updateVendorUserDTO){
        return adminInfoService.updateAdminInfo(updateVendorUserDTO);
    }
}

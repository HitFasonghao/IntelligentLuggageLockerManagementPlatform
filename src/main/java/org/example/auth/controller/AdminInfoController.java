package org.example.auth.controller;

import jakarta.validation.Valid;
import org.example.auth.dto.UpdateAdminDTO;
import org.example.auth.service.AdminInfoService;
import org.example.auth.vo.AdminInfoVO;
import org.example.auth.vo.HttpResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @GetMapping("/queryUserInfo")
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

    /**
     * 更换头像
     */
    @PostMapping("/updateAvatar")
    public HttpResponseVO<String> updateAvatar(@RequestParam("file") MultipartFile file){
        return adminInfoService.updateAvatar(file);
    }
}

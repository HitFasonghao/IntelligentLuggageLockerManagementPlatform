package org.example.auth.controller;


import jakarta.validation.Valid;
import org.example.auth.dto.*;
import org.example.auth.service.impl.LoginServiceImpl;
import org.example.auth.vo.AccessTokenVO;
import org.example.auth.vo.HttpResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 登录模块
 * @author fasonghao
 */
@RestController
@RequestMapping("/login")
public class LoginController {
    @Autowired
    private LoginServiceImpl loginService;

    /**
     * 账密登录
     */
    @PostMapping("/loginByPassword")
    public HttpResponseVO<AccessTokenVO> loginByPassword(@RequestBody @Valid LoginByPasswordDTO loginByPasswordDTO){
        return loginService.loginByPassword(loginByPasswordDTO);
    }

    /**
     * 短信验证码登录
     */
    @PostMapping("/loginBySmsCode")
    public HttpResponseVO<AccessTokenVO> loginBySmsCode(@RequestBody @Valid LoginBySmsCodeDTO loginBySmsCodeDTO){
        return loginService.loginBySmsCode(loginBySmsCodeDTO);
    }

    /**
     * 发送短信验证码
     */
    @PostMapping("/smsCode")
    public HttpResponseVO<String> sendSmsCode(@RequestBody @Valid SendSmsCodeDTO sendSmsCodeDTO){
        return loginService.sendSmsCode(sendSmsCodeDTO);
    }

    /**
     * 修改账号密码
     */
    @PutMapping("/updatePassword")
    public HttpResponseVO<String> updatePassword(@RequestBody @Valid UpdatePasswordDTO updatePasswordDTO){
        return loginService.updatePassword(updatePasswordDTO);
    }

    /**
     * 确认原手机号的验证码
     */
    @PostMapping("/confirmOldPhone")
    public HttpResponseVO<String> confirmOldPhone(@RequestBody @Valid ConfirmOldPhoneDTO confirmOldPhoneDTO){
        return loginService.confirmOldPhone(confirmOldPhoneDTO);
    }

    /**
     * 更换绑定的手机号
     */
    @PutMapping("/updatePhone")
    public HttpResponseVO<String> updatePhone(@RequestBody @Valid UpdatePhoneDTO updatePhoneDTO){
        return loginService.updatePhone(updatePhoneDTO);
    }

    /**
     * 用户微信小程序登录
     */
}

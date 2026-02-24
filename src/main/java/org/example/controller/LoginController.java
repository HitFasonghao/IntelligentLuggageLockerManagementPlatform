package org.example.controller;


import org.example.dto.SmsCodeDTO;
import org.example.service.impl.LoginServiceImpl;
import org.example.vo.HttpResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public HttpResponseVO<String> loginByPassword(){
        return loginService.loginByPassword();
    }

    /**
     * 短信验证码登录
     */
    @PostMapping("/loginBySmsCode")
    public HttpResponseVO<String> loginBySmsCode(){
        return loginService.loginBySmsCode();
    }

    /**
     * 发送短信验证码
     */
    @PostMapping("/smsCode")
    public HttpResponseVO<String> sendSmsCode(@RequestBody SmsCodeDTO smsCodeDTO){
        return loginService.sendSmsCode(smsCodeDTO);
    }

    /**
     * 用户微信小程序登录
     */
}

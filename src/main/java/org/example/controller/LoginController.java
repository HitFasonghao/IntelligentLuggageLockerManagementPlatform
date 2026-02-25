package org.example.controller;


import jakarta.validation.Valid;
import org.example.dto.LoginByPasswordDTO;
import org.example.dto.LoginBySmsCodeDTO;
import org.example.dto.SendSmsCodeDTO;
import org.example.service.impl.LoginServiceImpl;
import org.example.vo.AccessTokenVO;
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
     * 用户微信小程序登录
     */
}

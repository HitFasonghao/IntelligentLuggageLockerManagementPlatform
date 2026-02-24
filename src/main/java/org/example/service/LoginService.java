package org.example.service;

import org.example.dto.SmsCodeDTO;
import org.example.vo.HttpResponseVO;
import org.springframework.web.bind.annotation.PostMapping;

public interface LoginService {
    /**
     * 账密登录
     */
    HttpResponseVO<String> loginByPassword();

    /**
     * 短信验证码登录
     */
    HttpResponseVO<String> loginBySmsCode();

    /**
     * 发送短信验证码
     */
    HttpResponseVO<String> sendSmsCode(SmsCodeDTO smsCodeDTO);

    /**
     * 用户微信小程序登录
     */
}

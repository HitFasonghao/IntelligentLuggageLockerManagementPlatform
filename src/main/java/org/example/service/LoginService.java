package org.example.service;

import org.example.dto.LoginByPasswordDTO;
import org.example.dto.LoginBySmsCodeDTO;
import org.example.dto.SendSmsCodeDTO;
import org.example.vo.AccessTokenVO;
import org.example.vo.HttpResponseVO;

/**
 * @author fasonghao
 */
public interface LoginService {
    /**
     * 账密登录
     */
    HttpResponseVO<AccessTokenVO> loginByPassword(LoginByPasswordDTO loginByPasswordDTO);

    /**
     * 短信验证码登录
     */
    HttpResponseVO<AccessTokenVO> loginBySmsCode(LoginBySmsCodeDTO loginBySmsCodeDTO);

    /**
     * 发送短信验证码
     */
    HttpResponseVO<String> sendSmsCode(SendSmsCodeDTO sendSmsCodeDTO);

    /**
     * 用户微信小程序登录
     */
}

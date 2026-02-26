package org.example.auth.service;

import jakarta.validation.Valid;
import org.example.auth.dto.*;
import org.example.auth.vo.AccessTokenVO;
import org.example.auth.vo.HttpResponseVO;

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
     * 修改账号密码
     */
    HttpResponseVO<String> updatePassword(@Valid UpdatePasswordDTO updatePasswordDTO);

    /**
     * 确认原手机号的验证码
     */
    HttpResponseVO<String> confirmOldPhone(@Valid ConfirmOldPhoneDTO confirmOldPhoneDTO);

    /**
     * 更换绑定的手机号
     */
    HttpResponseVO<String> updatePhone(@Valid UpdatePhoneDTO updatePhoneDTO);

    /**
     * 用户微信小程序登录
     */
}

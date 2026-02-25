package org.example.enums;

import lombok.Getter;
import org.example.constants.Constants;

/**
 * 短信验证码用途枚举类
 * @author fasonghao
 */

@Getter
public enum SmsCodePurposeEnum {
    LOGIN(Constants.LOGIN_SMS_CODE_PREFIX),
    UPDATE_PASSWORD(Constants.UPDATE_PASSWORD_SMS_CODE_PREFIX),
    UPDATE_PHONE(Constants.UPDATE_PHONE_SMS_CODE_PREFIX);

    private final String name;

    SmsCodePurposeEnum(String name){
        this.name=name;
    }
}

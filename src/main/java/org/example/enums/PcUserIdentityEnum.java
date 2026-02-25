package org.example.enums;

import lombok.Getter;

/**
 * @author fasonghao
 */
@Getter
public enum PcUserIdentityEnum {
    //超级管理员
    SUPER_ADMIN(1),
    //普通管理员
    ORDINARY_ADMIN(2),
    //厂商用户
    VENDOR_USER(3);

    private final int code;

    PcUserIdentityEnum(int code){
        this.code=code;
    }
}

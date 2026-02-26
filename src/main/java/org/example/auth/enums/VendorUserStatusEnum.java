package org.example.auth.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * @author fasonghao
 */

@Getter
public enum VendorUserStatusEnum {
    ACTIVE("active"),
    LOCKED("locked"),
    INACTIVE("inactive");

    @EnumValue
    private final String value;

    VendorUserStatusEnum(String value) {
        this.value = value;
    }
}

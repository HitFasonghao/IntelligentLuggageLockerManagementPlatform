package org.example.auth.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
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
    @JsonValue
    private final String value;

    VendorUserStatusEnum(String value) {
        this.value = value;
    }
}

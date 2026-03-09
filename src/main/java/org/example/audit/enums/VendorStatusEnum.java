package org.example.audit.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 厂商状态枚举
 * @author fasonghao
 */
@Getter
public enum VendorStatusEnum {
    DRAFT("draft"),
    PENDING("pending"),
    TESTING("testing"),
    APPROVED("approved"),
    REJECTED("rejected"),
    SUSPENDED("suspended"),
    BANNED("banned");

    @EnumValue
    @JsonValue
    private final String value;

    VendorStatusEnum(String value) {
        this.value = value;
    }
}

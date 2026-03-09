package org.example.audit.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 审核结果枚举
 * @author fasonghao
 */
@Getter
public enum AuditResultEnum {
    PASS("pass"),
    FAIL("fail"),
    PENDING("pending");

    @EnumValue
    @JsonValue
    private final String value;

    AuditResultEnum(String value) {
        this.value = value;
    }
}

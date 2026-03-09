package org.example.audit.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 审核类型枚举
 * @author fasonghao
 */
@Getter
public enum AuditTypeEnum {
    INITIAL("initial"),
    RENEWAL("renewal"),
    CHANGE("change"),
    COMPLAINT("complaint");

    @EnumValue
    @JsonValue
    private final String value;

    AuditTypeEnum(String value) {
        this.value = value;
    }
}

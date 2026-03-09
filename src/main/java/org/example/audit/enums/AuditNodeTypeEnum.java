package org.example.audit.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 审核流程节点类型枚举
 * @author fasonghao
 */
@Getter
public enum AuditNodeTypeEnum {
    QUALIFICATION("qualification"),
    API_TEST("api_test"),
    PERFORMANCE("performance"),
    COMPLIANCE("compliance"),
    MANUAL_REVIEW("manual_review");

    @EnumValue
    @JsonValue
    private final String value;

    AuditNodeTypeEnum(String value) {
        this.value = value;
    }
}

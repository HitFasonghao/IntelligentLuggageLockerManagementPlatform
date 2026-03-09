package org.example.audit.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 审核任务状态枚举
 * @author fasonghao
 */
@Getter
public enum AuditTaskStatusEnum {
    PENDING("pending"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    OVERDUE("overdue");

    @EnumValue
    @JsonValue
    private final String value;

    AuditTaskStatusEnum(String value) {
        this.value = value;
    }
}

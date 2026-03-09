package org.example.audit.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 审核任务优先级枚举
 * @author fasonghao
 */
@Getter
public enum AuditTaskPriorityEnum {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high"),
    URGENT("urgent");

    @EnumValue
    @JsonValue
    private final String value;

    AuditTaskPriorityEnum(String value) {
        this.value = value;
    }
}

package org.example.audit.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 测试结果枚举
 * @author fasonghao
 */
@Getter
public enum TestResultEnum {
    PENDING("pending"),
    TESTING("testing"),
    PASSED("passed"),
    FAILED("failed");

    @EnumValue
    @JsonValue
    private final String value;

    TestResultEnum(String value) {
        this.value = value;
    }
}

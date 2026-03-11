package org.example.device.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ClusterStatusEnum {
    USING("using"),
    FORBIDDEN("forbidden");

    @EnumValue
    @JsonValue
    private final String value;

    ClusterStatusEnum(String value) {
        this.value = value;
    }
}

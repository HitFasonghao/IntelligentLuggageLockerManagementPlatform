package org.example.device.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum CabinetStatusEnum {
    FREE("free"),
    OPENING("opening"),
    USING("using"),
    FORBIDDEN("forbidden");

    @EnumValue
    @JsonValue
    private final String value;

    CabinetStatusEnum(String value) {
        this.value = value;
    }
}

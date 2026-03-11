package org.example.device.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum TimeUnitEnum {
    AN_HOUR("anHour"),
    HALF_AN_HOUR("halfAnHour"),
    TEN_MINUTES("tenMinutes");

    @EnumValue
    @JsonValue
    private final String value;

    TimeUnitEnum(String value) {
        this.value = value;
    }
}

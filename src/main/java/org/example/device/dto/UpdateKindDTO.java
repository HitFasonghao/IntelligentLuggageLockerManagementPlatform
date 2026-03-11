package org.example.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.device.enums.TimeUnitEnum;

import java.math.BigDecimal;

@Data
public class UpdateKindDTO {

    @NotNull(message = "种类id不能为空")
    private Integer kindId;

    @NotBlank(message = "种类名称不能为空")
    private String name;

    @NotBlank(message = "描述不能为空")
    private String description;

    @NotNull(message = "收费金额不能为空")
    private BigDecimal charge;

    @NotNull(message = "收费时间单位不能为空")
    private TimeUnitEnum timeUnit;
}

package org.example.device.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignKindDTO {

    @NotNull(message = "寄存柜id不能为空")
    private Integer cabinetId;

    @NotNull(message = "种类id不能为空")
    private Integer kindId;
}

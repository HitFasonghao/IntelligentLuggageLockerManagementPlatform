package org.example.device.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignNumberDTO {

    @NotNull(message = "寄存柜id不能为空")
    private Integer cabinetId;

    private Integer number;
}

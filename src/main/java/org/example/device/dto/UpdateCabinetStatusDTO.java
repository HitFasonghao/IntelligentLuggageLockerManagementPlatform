package org.example.device.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.device.enums.CabinetStatusEnum;

@Data
public class UpdateCabinetStatusDTO {

    @NotNull(message = "寄存柜id不能为空")
    private Integer cabinetId;

    @NotNull(message = "目标状态不能为空")
    private CabinetStatusEnum status;
}

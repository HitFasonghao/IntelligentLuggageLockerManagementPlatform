package org.example.device.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignClusterDTO {

    @NotNull(message = "寄存柜id不能为空")
    private Integer cabinetId;

    @NotNull(message = "柜群id不能为空")
    private Integer clusterId;
}

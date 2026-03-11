package org.example.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateClusterDTO {

    @NotBlank(message = "柜群名称不能为空")
    private String name;

    @NotBlank(message = "柜群地址不能为空")
    private String location;

    @NotNull(message = "经度不能为空")
    private BigDecimal longitude;

    @NotNull(message = "纬度不能为空")
    private BigDecimal dimension;

    @NotBlank(message = "描述不能为空")
    private String description;
}

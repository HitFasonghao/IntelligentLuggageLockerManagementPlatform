package org.example.audit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.example.audit.enums.AuditNodeTypeEnum;

/**
 * 创建审核流程节点 DTO
 * @author fasonghao
 */
@Data
public class CreateAuditNodeDTO {

    @NotBlank(message = "节点名称不能为空")
    @Size(max = 50, message = "节点名称长度不能超过50")
    private String name;

    @NotNull(message = "节点类型不能为空")
    private AuditNodeTypeEnum type;

    @NotNull(message = "顺序不能为空")
    private Integer order;

    private Boolean autoPass;

    private Integer timeoutHours;

    private Boolean isActive;
}

package org.example.audit.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import org.example.audit.enums.AuditNodeTypeEnum;

/**
 * 更新审核流程节点 DTO
 * @author fasonghao
 */
@Data
public class UpdateAuditNodeDTO {

    @Size(max = 50, message = "节点名称长度不能超过50")
    private String name;

    private AuditNodeTypeEnum type;

    private Integer order;

    private Boolean autoPass;

    private Integer timeoutHours;

    private Boolean isActive;
}

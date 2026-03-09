package org.example.audit.dto;

import lombok.Data;
import org.example.audit.enums.AuditTaskPriorityEnum;
import org.example.audit.enums.AuditTaskStatusEnum;

/**
 * 更新审核任务 DTO
 * @author fasonghao
 */
@Data
public class UpdateAuditTaskDTO {

    private AuditTaskStatusEnum status;

    private AuditTaskPriorityEnum priority;

    private String notes;
}

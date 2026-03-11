package org.example.audit.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.audit.enums.AuditTaskPriorityEnum;

import java.time.LocalDateTime;

/**
 * 分配审核任务 DTO
 * @author fasonghao
 */
@Data
public class AssignAuditTaskDTO {

    @NotNull(message = "审核任务ID不能为空")
    private Integer auditTaskId;

    @NotNull(message = "审核节点ID不能为空")
    private Integer auditNodeId;

    @NotNull(message = "审核员ID不能为空")
    private Integer adminId;

    private AuditTaskPriorityEnum priority;

    @NotNull(message = "截止时间不能为空")
    private LocalDateTime dueDate;
}

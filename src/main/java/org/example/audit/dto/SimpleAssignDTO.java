package org.example.audit.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.audit.enums.AuditTaskPriorityEnum;

import java.time.LocalDateTime;

/**
 * 简化的任务分配DTO（审核任务分配页面使用）
 * @author fasonghao
 */
@Data
public class SimpleAssignDTO {

    @NotNull(message = "厂商ID不能为空")
    private Integer vendorId;

    @NotNull(message = "审核员ID不能为空")
    private Integer adminId;

    private AuditTaskPriorityEnum priority;

    @NotNull(message = "截止时间不能为空")
    private LocalDateTime dueDate;
}

package org.example.audit.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.example.audit.enums.AuditTaskPriorityEnum;
import org.example.audit.enums.AuditTaskStatusEnum;

import java.time.LocalDateTime;

/**
 * 审核任务表
 * 对应表：audit_tasks
 * @author fasonghao
 */
@Data
@TableName("audit_tasks")
public class AuditTaskPO {

    @TableId(value = "audit_task_id", type = IdType.AUTO)
    private Integer auditTaskId;

    @TableField("vendor_id")
    private Integer vendorId;

    @TableField("audit_record_id")
    private Integer auditRecordId;

    @TableField("audit_node_id")
    private Integer auditNodeId;

    @TableField("admin_id")
    private Integer adminId;

    @TableField("status")
    private AuditTaskStatusEnum status;

    @TableField("priority")
    private AuditTaskPriorityEnum priority;

    @TableField("due_date")
    private LocalDateTime dueDate;

    @TableField("completed_time")
    private LocalDateTime completedTime;

    @TableField("notes")
    private String notes;

    @TableField("passed")
    private Boolean passed;

    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}

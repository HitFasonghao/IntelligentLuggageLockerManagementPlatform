package org.example.audit.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.example.audit.enums.AuditTaskPriorityEnum;
import org.example.audit.enums.AuditTaskStatusEnum;

import java.time.LocalDateTime;

/**
 * 审核任务分配表
 * 对应表：audit_tasks
 * @author fasonghao
 */
@Data
@TableName("audit_tasks")
public class AuditTaskPO {

    @TableId(value = "audit_task_id", type = IdType.AUTO)
    private Integer auditTaskId;

    @TableField("audit_instance_id")
    private Integer auditInstanceId;

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

    @TableField("result")
    private String result;
}

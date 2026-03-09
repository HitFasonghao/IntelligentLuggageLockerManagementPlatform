package org.example.audit.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核流程实例表
 * 对应表：audit_instances
 * @author fasonghao
 */
@Data
@TableName("audit_instances")
public class AuditInstancePO {

    @TableId(value = "audit_instance_id", type = IdType.AUTO)
    private Integer auditInstanceId;

    @TableField("vendor_id")
    private Integer vendorId;

    @TableField("audit_record_id")
    private Integer auditRecordId;

    @TableField("audit_node_id")
    private Integer auditNodeId;

    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField("started_time")
    private LocalDateTime startedTime;

    @TableField("completed_time")
    private LocalDateTime completedTime;
}

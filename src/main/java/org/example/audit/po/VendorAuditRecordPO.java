package org.example.audit.po;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import org.example.audit.enums.AuditRecordResultEnum;
import org.example.audit.enums.AuditTypeEnum;

import java.time.LocalDateTime;

/**
 * 审核记录表
 * 对应表：vendor_audit_records
 * @author fasonghao
 */
@Data
@TableName(value = "vendor_audit_records", autoResultMap = true)
public class VendorAuditRecordPO {

    @TableId(value = "audit_record_id", type = IdType.AUTO)
    private Integer auditRecordId;

    @TableField("vendor_id")
    private Integer vendorId;

    /** 提交申请的厂商用户ID */
    @TableField("vendor_user_id")
    private Integer vendorUserId;

    @TableField("round")
    private Integer round;

    @TableField("type")
    private AuditTypeEnum type;

    @TableField(value = "data", typeHandler = JacksonTypeHandler.class)
    private Object data;

    @TableField("admin_id")
    private Integer adminId;

    @TableField("result")
    private AuditRecordResultEnum result;

    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField("completed_time")
    private LocalDateTime completedTime;
}

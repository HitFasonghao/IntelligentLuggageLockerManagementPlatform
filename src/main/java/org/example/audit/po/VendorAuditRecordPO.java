package org.example.audit.po;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import org.example.audit.enums.AuditResultEnum;
import org.example.audit.enums.AuditTypeEnum;
import org.example.audit.enums.TestResultEnum;

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

    @TableField("round")
    private Integer round;

    @TableField("type")
    private AuditTypeEnum type;

    @TableField(value = "data", typeHandler = JacksonTypeHandler.class)
    private Object data;

    @TableField("admin_id")
    private Integer adminId;

    @TableField("audit_notes")
    private String auditNotes;

    @TableField("audit_result")
    private AuditResultEnum auditResult;

    @TableField("test_result")
    private TestResultEnum testResult;

    @TableField("test_notes")
    private String testNotes;

    @TableField("test_started_time")
    private LocalDateTime testStartedTime;

    @TableField("test_completed_time")
    private LocalDateTime testCompletedTime;

    @TableField(value = "api_validation_result", typeHandler = JacksonTypeHandler.class)
    private Object apiValidationResult;

    @TableField(value = "performance_result", typeHandler = JacksonTypeHandler.class)
    private Object performanceResult;

    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField("completed_time")
    private LocalDateTime completedTime;
}

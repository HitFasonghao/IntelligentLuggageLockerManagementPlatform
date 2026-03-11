package org.example.audit.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核流程节点进度VO（从audit_tasks中获取）
 * @author fasonghao
 */
@Data
public class AuditInstanceVO {

    private Integer auditTaskId;
    private Integer vendorId;
    private Integer auditRecordId;
    private Integer auditNodeId;
    private String nodeName;
    private String nodeType;
    private LocalDateTime createdTime;
    private LocalDateTime completedTime;
    /** 是否通过 */
    private Boolean passed;
    /** 审核意见 */
    private String notes;
    /** 审核管理员名称 */
    private String adminName;
}

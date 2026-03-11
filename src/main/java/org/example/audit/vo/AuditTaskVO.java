package org.example.audit.vo;

import lombok.Data;
import org.example.audit.enums.AuditNodeTypeEnum;
import org.example.audit.enums.AuditTaskPriorityEnum;
import org.example.audit.enums.AuditTaskStatusEnum;

import java.time.LocalDateTime;

/**
 * 审核任务VO
 * @author fasonghao
 */
@Data
public class AuditTaskVO {

    private Integer auditTaskId;
    private Integer vendorId;
    private Integer auditRecordId;
    private Integer auditNodeId;
    private String nodeName;
    /** 节点类型，前端据此展示不同的审核表单 */
    private AuditNodeTypeEnum nodeType;
    private Integer adminId;
    private AuditTaskStatusEnum status;
    private AuditTaskPriorityEnum priority;
    private LocalDateTime dueDate;
    private LocalDateTime completedTime;
    private String notes;
    /** 是否通过 */
    private Boolean passed;
    /** 审核管理员名称 */
    private String adminName;
    /** 关联的厂商名称，列表展示用 */
    private String companyName;
    /** 审核轮次 */
    private Integer round;
}

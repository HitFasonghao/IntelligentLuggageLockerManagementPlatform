package org.example.audit.vo;

import lombok.Data;
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
    private Integer auditInstanceId;
    private Integer auditNodeId;
    private String nodeName;
    private Integer adminId;
    private AuditTaskStatusEnum status;
    private AuditTaskPriorityEnum priority;
    private LocalDateTime dueDate;
    private LocalDateTime completedTime;
    private String notes;
    /** 关联的厂商名称，列表展示用 */
    private String companyName;
    /** 关联的厂商ID */
    private Integer vendorId;
}

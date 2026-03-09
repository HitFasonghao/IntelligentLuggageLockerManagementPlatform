package org.example.audit.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核流程实例VO
 * @author fasonghao
 */
@Data
public class AuditInstanceVO {

    private Integer auditInstanceId;
    private Integer vendorId;
    private Integer auditRecordId;
    private Integer auditNodeId;
    private String nodeName;
    private String nodeType;
    private LocalDateTime createdTime;
    private LocalDateTime startedTime;
    private LocalDateTime completedTime;
}

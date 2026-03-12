package org.example.audit.dto;

import lombok.Data;
import org.example.audit.enums.AuditTaskPriorityEnum;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class AuditTaskQueryDTO {
    private String companyName;
    private Integer auditNodeId;
    private AuditTaskPriorityEnum priority;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dueDateStart;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dueDateEnd;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}

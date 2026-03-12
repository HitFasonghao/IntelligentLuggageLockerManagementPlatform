package org.example.audit.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class AuditRecordQueryDTO {
    private String companyName;
    private Integer round;
    private Integer auditNodeId;
    private Boolean passed;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime completedTimeStart;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime completedTimeEnd;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}

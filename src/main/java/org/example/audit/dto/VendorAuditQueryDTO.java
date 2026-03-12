package org.example.audit.dto;

import lombok.Data;
import org.example.audit.enums.AuditRecordResultEnum;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class VendorAuditQueryDTO {
    private String companyName;
    private AuditRecordResultEnum result;
    private Integer round;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime submitTimeStart;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime submitTimeEnd;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime completedTimeStart;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime completedTimeEnd;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}

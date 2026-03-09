package org.example.audit.vo;

import lombok.Data;
import org.example.audit.enums.AuditResultEnum;
import org.example.audit.enums.AuditTypeEnum;
import org.example.audit.enums.TestResultEnum;

import java.time.LocalDateTime;

/**
 * 审核记录VO
 * @author fasonghao
 */
@Data
public class VendorAuditRecordVO {

    private Integer auditRecordId;
    private Integer vendorId;
    private Integer round;
    private AuditTypeEnum type;
    private Object data;
    private Integer adminId;
    private String auditNotes;
    private AuditResultEnum auditResult;
    private TestResultEnum testResult;
    private String testNotes;
    private LocalDateTime testStartedTime;
    private LocalDateTime testCompletedTime;
    private Object apiValidationResult;
    private Object performanceResult;
    private LocalDateTime createdTime;
    private LocalDateTime completedTime;
}

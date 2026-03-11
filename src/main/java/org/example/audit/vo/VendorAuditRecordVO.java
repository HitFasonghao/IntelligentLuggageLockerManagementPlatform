package org.example.audit.vo;

import lombok.Data;
import org.example.audit.enums.AuditRecordResultEnum;
import org.example.audit.enums.AuditTypeEnum;

import java.time.LocalDateTime;

/**
 * 审核记录VO
 * @author fasonghao
 */
@Data
public class VendorAuditRecordVO {

    private Integer auditRecordId;
    private Integer vendorId;
    /** 厂商名称（列表展示用） */
    private String companyName;
    private Integer round;
    private AuditTypeEnum type;
    private Object data;
    private Integer adminId;
    /** 审核管理员名称 */
    private String adminName;
    /** 审核进度结果 */
    private AuditRecordResultEnum result;
    private LocalDateTime createdTime;
    private LocalDateTime completedTime;
}

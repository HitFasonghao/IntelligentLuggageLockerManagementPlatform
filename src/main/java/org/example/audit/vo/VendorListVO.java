package org.example.audit.vo;

import lombok.Data;
import org.example.audit.enums.AuditRecordResultEnum;
import org.example.audit.enums.VendorStatusEnum;

import java.time.LocalDateTime;

/**
 * 厂商列表项VO（管理员审核列表展示用）
 * @author fasonghao
 */
@Data
public class VendorListVO {

    private Integer vendorId;
    /** 审核记录ID */
    private Integer auditRecordId;
    private String companyName;
    private String shortName;
    private String contactPerson;
    private String contactPhone;
    private VendorStatusEnum status;
    /** 审核进度结果 */
    private AuditRecordResultEnum result;
    private Integer currentRound;
    private LocalDateTime submittedTime;
    private LocalDateTime createdTime;
    private LocalDateTime completedTime;
    /** 申请用户名称 */
    private String vendorUserName;
    /** 是否可重新申请 */
    private Boolean canResubmit;
}

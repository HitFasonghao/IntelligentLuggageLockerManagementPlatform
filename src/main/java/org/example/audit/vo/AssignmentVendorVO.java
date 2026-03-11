package org.example.audit.vo;

import lombok.Data;
import org.example.audit.enums.VendorStatusEnum;

import java.time.LocalDateTime;

/**
 * 待分配/已分配厂商VO
 * @author fasonghao
 */
@Data
public class AssignmentVendorVO {
    private Integer vendorId;
    private String companyName;
    private String shortName;
    private String contactPerson;
    private String contactPhone;
    private VendorStatusEnum status;
    private LocalDateTime submittedTime;
    private Integer currentRound;

    /** 当前活跃的审核任务ID */
    private Integer auditTaskId;
    /** 当前活跃的审核节点ID */
    private Integer auditNodeId;
    /** 当前节点名称 */
    private String currentNodeName;

    /** 已分配的管理员ID（null表示未分配） */
    private Integer assignedAdminId;
    /** 已分配的管理员姓名 */
    private String assignedAdminName;
}

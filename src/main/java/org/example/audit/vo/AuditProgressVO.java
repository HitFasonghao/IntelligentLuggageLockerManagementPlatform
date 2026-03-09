package org.example.audit.vo;

import lombok.Data;

import java.util.List;

/**
 * 审核进度VO（厂商用户查看审核进度用）
 * @author fasonghao
 */
@Data
public class AuditProgressVO {

    /** 当前审核轮次 */
    private Integer currentRound;
    /** 厂商当前状态 */
    private String vendorStatus;
    /** 审核记录 */
    private VendorAuditRecordVO auditRecord;
    /** 流程节点实例列表（按顺序） */
    private List<AuditInstanceVO> instances;
}

package org.example.audit.statemachine;

import lombok.Data;
import org.example.audit.po.VendorAuditRecordPO;
import org.example.audit.po.VendorPO;
import org.example.auth.common.PcUserInfo;

/**
 * 审核状态机上下文，承载一次状态转移所需的全部信息
 * @author fasonghao
 */
@Data
public class AuditContext {
    /** 当前操作的厂商 */
    private VendorPO vendor;
    /** 最新的审核记录（提交类事件时为null） */
    private VendorAuditRecordPO latestRecord;
    /** 当前登录用户 */
    private PcUserInfo userInfo;
    /** 业务数据（各DTO） */
    private Object dto;
}

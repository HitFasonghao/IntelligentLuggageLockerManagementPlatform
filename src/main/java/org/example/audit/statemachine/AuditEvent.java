package org.example.audit.statemachine;

/**
 * 审核流程事件枚举
 * @author fasonghao
 */
public enum AuditEvent {
    /** 提交入驻申请 */
    SUBMIT,
    /** 资质审核通过 */
    QUALIFICATION_PASS,
    /** 资质审核不通过 */
    QUALIFICATION_FAIL,
    /** 功能测试通过 */
    FUNCTIONAL_TEST_PASS,
    /** 功能测试失败 */
    FUNCTIONAL_TEST_FAIL,
    /** 性能测试通过 */
    PERFORMANCE_PASS,
    /** 性能测试失败 */
    PERFORMANCE_FAIL,
    /** 最终审批通过 */
    FINAL_APPROVE,
    /** 最终审批驳回 */
    FINAL_REJECT,
    /** 驳回后重新提交 */
    RESUBMIT,
    /** 暂停合作 */
    SUSPEND,
    /** 恢复合作 */
    RESTORE,
    /** 封禁 */
    BAN
}

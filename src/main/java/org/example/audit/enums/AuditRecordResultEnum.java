package org.example.audit.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 审核记录结果枚举（跟踪审核进度状态）
 * @author fasonghao
 */
@Getter
public enum AuditRecordResultEnum {
    NOT_STARTED("not_started", "未开始"),
    PENDING_QUALIFICATION("pending_qualification", "待资质审核"),
    QUALIFICATION_FAILED("qualification_failed", "资质审核失败"),
    PENDING_FUNCTIONAL_TEST("pending_functional_test", "待功能测试"),
    FUNCTIONAL_TEST_FAILED("functional_test_failed", "功能测试失败"),
    PENDING_PERFORMANCE_TEST("pending_performance_test", "待性能测试"),
    PERFORMANCE_TEST_FAILED("performance_test_failed", "性能测试失败"),
    PENDING_FINAL_APPROVAL("pending_final_approval", "待最终审批"),
    FINAL_APPROVAL_REJECTED("final_approval_rejected", "最终审批驳回"),
    APPROVED("approved", "审核通过");

    @EnumValue
    @JsonValue
    private final String value;

    private final String label;

    AuditRecordResultEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }
}

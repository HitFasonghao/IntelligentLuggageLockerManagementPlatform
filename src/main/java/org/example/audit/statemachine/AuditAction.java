package org.example.audit.statemachine;

/**
 * 审核动作接口
 * @author fasonghao
 */
@FunctionalInterface
public interface AuditAction {
    /**
     * 执行动作
     * @param context 审核上下文
     * @return 成功提示信息
     */
    String execute(AuditContext context);
}

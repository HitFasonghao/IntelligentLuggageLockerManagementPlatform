package org.example.audit.statemachine;

import jakarta.annotation.PostConstruct;
import org.example.audit.enums.VendorStatusEnum;
import org.example.audit.mapper.VendorMapper;
import org.example.audit.po.VendorPO;
import org.example.auth.common.PcUserInfo;
import org.example.auth.common.UserContext;
import org.example.auth.constants.HttpStatusConstants;
import org.example.auth.vo.HttpResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 审核状态机，集中管理所有状态转移规则
 *
 * <p>扩展指南：新增审核步骤只需三步：
 * <ol>
 *   <li>在 {@link AuditEvent} 中添加新事件</li>
 *   <li>在 {@link AuditActions} 中实现动作方法</li>
 *   <li>在 {@link #init()} 的转移表中注册规则</li>
 * </ol>
 *
 * @author fasonghao
 */
@Component
public class AuditStateMachine {

    @Autowired
    private AuditActions actions;

    @Autowired
    private AuditNodeHelper nodeHelper;

    @Autowired
    private VendorMapper vendorMapper;

    /**
     * 转移表：(当前状态, 事件) → 转移定义
     */
    private final Map<StateEventKey, Transition> transitionTable = new HashMap<>();

    @PostConstruct
    public void init() {
        // ========== 审核流程 ==========
        // 厂商端：提交 / 重新提交
        register(VendorStatusEnum.DRAFT,     AuditEvent.SUBMIT,              VendorStatusEnum.PENDING,   actions::onSubmit);
        register(VendorStatusEnum.REJECTED,  AuditEvent.RESUBMIT,            VendorStatusEnum.PENDING,   actions::onResubmit);

        // 管理员端：资质审核
        register(VendorStatusEnum.PENDING,   AuditEvent.QUALIFICATION_PASS,  VendorStatusEnum.TESTING,   actions::onQualificationPass);
        register(VendorStatusEnum.PENDING,   AuditEvent.QUALIFICATION_FAIL,  VendorStatusEnum.REJECTED,  actions::onQualificationFail);

        // 管理员端：功能测试
        register(VendorStatusEnum.TESTING,   AuditEvent.FUNCTIONAL_TEST_PASS, VendorStatusEnum.TESTING,   actions::onFunctionalTestPass);
        register(VendorStatusEnum.TESTING,   AuditEvent.FUNCTIONAL_TEST_FAIL, VendorStatusEnum.REJECTED,  actions::onFunctionalTestFail);

        // 管理员端：性能测试
        register(VendorStatusEnum.TESTING,   AuditEvent.PERFORMANCE_PASS,    VendorStatusEnum.TESTING,   actions::onPerformancePass);
        register(VendorStatusEnum.TESTING,   AuditEvent.PERFORMANCE_FAIL,    VendorStatusEnum.REJECTED,  actions::onPerformanceFail);

        // 管理员端：最终审批
        register(VendorStatusEnum.TESTING,   AuditEvent.FINAL_APPROVE,       VendorStatusEnum.APPROVED,  actions::onFinalApprove);
        register(VendorStatusEnum.TESTING,   AuditEvent.FINAL_REJECT,        VendorStatusEnum.REJECTED,  actions::onFinalReject);

        // ========== 厂商管理 ==========
        register(VendorStatusEnum.APPROVED,  AuditEvent.SUSPEND,             VendorStatusEnum.SUSPENDED, actions::onSuspend);
        register(VendorStatusEnum.SUSPENDED, AuditEvent.RESTORE,             VendorStatusEnum.APPROVED,  actions::onRestore);
        register(VendorStatusEnum.APPROVED,  AuditEvent.BAN,                 VendorStatusEnum.BANNED,    actions::onBan);
        register(VendorStatusEnum.SUSPENDED, AuditEvent.BAN,                 VendorStatusEnum.BANNED,    actions::onBan);
    }

    /**
     * 触发状态转移
     *
     * @param vendorId 厂商ID
     * @param event    触发事件
     * @param dto      业务数据（对应的DTO对象）
     * @return 操作结果
     */
    @Transactional
    public HttpResponseVO<String> fire(Integer vendorId, AuditEvent event, Object dto) {
        // 1. 查找厂商
        VendorPO vendor = vendorMapper.selectById(vendorId);
        if (vendor == null) {
            return error("厂商不存在");
        }

        // 2. 查找转移规则
        VendorStatusEnum currentStatus = vendor.getStatus();
        Transition transition = transitionTable.get(new StateEventKey(currentStatus, event));
        if (transition == null) {
            return error("当前状态[" + currentStatus.getValue() + "]不允许执行[" + event + "]操作");
        }

        // 3. 构建上下文
        AuditContext context = new AuditContext();
        context.setVendor(vendor);
        context.setUserInfo(UserContext.get());
        context.setDto(dto);
        // 非提交类事件，加载最新审核记录
        if (event != AuditEvent.SUBMIT) {
            context.setLatestRecord(nodeHelper.getLatestRecord(vendorId));
        }

        // 4. 执行动作
        String message = transition.action().execute(context);

        // 5. 更新厂商状态并保存
        vendor.setStatus(transition.targetStatus());
        vendorMapper.updateById(vendor);

        return HttpResponseVO.<String>builder()
                .code(HttpStatusConstants.SUCCESS)
                .msg(message)
                .build();
    }

    // ==================== 内部结构 ====================

    private void register(VendorStatusEnum from, AuditEvent event, VendorStatusEnum to, AuditAction action) {
        transitionTable.put(new StateEventKey(from, event), new Transition(to, action));
    }

    private HttpResponseVO<String> error(String msg) {
        return HttpResponseVO.<String>builder()
                .code(HttpStatusConstants.ERROR)
                .msg(msg)
                .build();
    }

    /**
     * 转移表的 Key：(当前状态, 事件)
     */
    private record StateEventKey(VendorStatusEnum status, AuditEvent event) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof StateEventKey that)) return false;
            return status == that.status && event == that.event;
        }

        @Override
        public int hashCode() {
            return Objects.hash(status, event);
        }
    }

    /**
     * 转移定义：目标状态 + 动作
     */
    private record Transition(VendorStatusEnum targetStatus, AuditAction action) {
    }
}

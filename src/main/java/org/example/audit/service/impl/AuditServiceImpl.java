package org.example.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.audit.dto.FinalApprovalDTO;
import org.example.audit.dto.PerformanceTestDTO;
import org.example.audit.dto.QualificationAuditDTO;
import org.example.audit.dto.TechTestAuditDTO;
import org.example.audit.enums.AuditResultEnum;
import org.example.audit.enums.AuditTaskStatusEnum;
import org.example.audit.enums.TestResultEnum;
import org.example.audit.enums.VendorStatusEnum;
import org.example.audit.mapper.*;
import org.example.audit.po.*;
import org.example.audit.service.AuditService;
import org.example.audit.statemachine.AuditEvent;
import org.example.audit.statemachine.AuditStateMachine;
import org.example.audit.vo.AuditTaskVO;
import org.example.audit.vo.VendorAuditRecordVO;
import org.example.audit.vo.VendorListVO;
import org.example.audit.vo.VendorVO;
import org.example.auth.common.PcUserInfo;
import org.example.auth.common.UserContext;
import org.example.auth.constants.HttpStatusConstants;
import org.example.auth.enums.PcUserIdentityEnum;
import org.example.auth.vo.HttpResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 审核服务实现（平台管理员端）
 * <p>审核流程相关操作统一委托给 {@link AuditStateMachine} 处理
 *
 * @author fasonghao
 */
@Service
public class AuditServiceImpl implements AuditService {

    @Autowired
    private VendorMapper vendorMapper;

    @Autowired
    private VendorAuditRecordMapper vendorAuditRecordMapper;

    @Autowired
    private AuditMapStructMapper mapStructMapper;

    @Autowired
    private AuditStateMachine stateMachine;

    @Autowired
    private AuditTaskMapper auditTaskMapper;

    @Autowired
    private AuditInstanceMapper auditInstanceMapper;

    @Autowired
    private AuditNodeMapper auditNodeMapper;

    /**
     * 获取厂商审核列表
     * <p>所有管理员只能看到已分配任务的厂商
     * <p>超级管理员：看到所有已分配（给任意管理员）的厂商
     * <p>普通管理员：只看到分配给自己的厂商
     */
    @Override
    public HttpResponseVO<List<VendorListVO>> getVendorList(VendorStatusEnum status) {
        PcUserInfo userInfo = UserContext.get();

        // 查询已分配任务的厂商ID集合（超管看所有已分配的，普管只看自己的）
        Set<Integer> assignedVendorIds = getAssignedVendorIds(userInfo);

        if (assignedVendorIds.isEmpty()) {
            return HttpResponseVO.<List<VendorListVO>>builder()
                    .data(List.of())
                    .code(HttpStatusConstants.SUCCESS)
                    .msg("获取厂商列表成功")
                    .build();
        }

        LambdaQueryWrapper<VendorPO> wrapper = Wrappers.lambdaQuery();
        wrapper.ne(VendorPO::getStatus, VendorStatusEnum.DRAFT);
        if (status != null) {
            wrapper.eq(VendorPO::getStatus, status);
        }
        wrapper.in(VendorPO::getVendorId, assignedVendorIds);

        wrapper.orderByDesc(VendorPO::getSubmittedTime);
        List<VendorPO> vendors = vendorMapper.selectList(wrapper);

        List<VendorListVO> voList = vendors.stream().map(v -> {
            VendorListVO vo = new VendorListVO();
            vo.setVendorId(v.getVendorId());
            vo.setCompanyName(v.getCompanyName());
            vo.setShortName(v.getShortName());
            vo.setContactPerson(v.getContactPerson());
            vo.setContactPhone(v.getContactPhone());
            vo.setStatus(v.getStatus());
            vo.setSubmittedTime(v.getSubmittedTime());
            vo.setCreatedTime(v.getCreatedTime());
            // 查询当前最大轮次
            LambdaQueryWrapper<VendorAuditRecordPO> recordWrapper = Wrappers.lambdaQuery();
            recordWrapper.eq(VendorAuditRecordPO::getVendorId, v.getVendorId());
            recordWrapper.orderByDesc(VendorAuditRecordPO::getRound);
            recordWrapper.last("LIMIT 1");
            VendorAuditRecordPO lastRecord = vendorAuditRecordMapper.selectOne(recordWrapper);
            vo.setCurrentRound(lastRecord != null ? lastRecord.getRound() : 0);
            return vo;
        }).collect(Collectors.toList());

        return HttpResponseVO.<List<VendorListVO>>builder()
                .data(voList)
                .code(HttpStatusConstants.SUCCESS)
                .msg("获取厂商列表成功")
                .build();
    }

    /**
     * 查询管理员被分配的厂商ID集合
     */
    private Set<Integer> getAssignedVendorIds(PcUserInfo userInfo) {
        // 所有角色都只查分配给自己的任务
        LambdaQueryWrapper<AuditTaskPO> taskWrapper = Wrappers.lambdaQuery();
        taskWrapper.eq(AuditTaskPO::getAdminId, userInfo.getUserId());
        List<AuditTaskPO> tasks = auditTaskMapper.selectList(taskWrapper);

        // 通过任务关联的实例，找到厂商ID
        Set<Integer> vendorIds = new HashSet<>();
        for (AuditTaskPO task : tasks) {
            AuditInstancePO instance = auditInstanceMapper.selectById(task.getAuditInstanceId());
            if (instance != null) {
                vendorIds.add(instance.getVendorId());
            }
        }
        return vendorIds;
    }

    /**
     * 获取厂商详情
     */
    @Override
    public HttpResponseVO<VendorVO> getVendorDetail(Integer vendorId) {
        VendorPO vendor = vendorMapper.selectById(vendorId);
        if (vendor == null) {
            return HttpResponseVO.<VendorVO>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("厂商不存在")
                    .build();
        }
        return HttpResponseVO.<VendorVO>builder()
                .data(mapStructMapper.vendorPoToVo(vendor))
                .code(HttpStatusConstants.SUCCESS)
                .msg("获取厂商详情成功")
                .build();
    }

    /**
     * 获取厂商的审核记录列表
     */
    @Override
    public HttpResponseVO<List<VendorAuditRecordVO>> getAuditRecords(Integer vendorId) {
        LambdaQueryWrapper<VendorAuditRecordPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(VendorAuditRecordPO::getVendorId, vendorId);
        wrapper.orderByDesc(VendorAuditRecordPO::getRound);
        List<VendorAuditRecordPO> records = vendorAuditRecordMapper.selectList(wrapper);

        List<VendorAuditRecordVO> voList = records.stream()
                .map(mapStructMapper::auditRecordPoToVo)
                .collect(Collectors.toList());

        return HttpResponseVO.<List<VendorAuditRecordVO>>builder()
                .data(voList)
                .code(HttpStatusConstants.SUCCESS)
                .msg("获取审核记录成功")
                .build();
    }

    /**
     * 获取当前管理员的审核完成记录（查询已完成的任务）
     */
    @Override
    public HttpResponseVO<List<AuditTaskVO>> getMyAuditRecords() {
        PcUserInfo userInfo = UserContext.get();

        LambdaQueryWrapper<AuditTaskPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AuditTaskPO::getAdminId, userInfo.getUserId());
        wrapper.eq(AuditTaskPO::getStatus, AuditTaskStatusEnum.COMPLETED);
        wrapper.orderByDesc(AuditTaskPO::getCompletedTime);
        List<AuditTaskPO> tasks = auditTaskMapper.selectList(wrapper);

        List<AuditTaskVO> voList = tasks.stream()
                .map(this::convertTaskToVO)
                .collect(Collectors.toList());

        return HttpResponseVO.<List<AuditTaskVO>>builder()
                .data(voList)
                .code(HttpStatusConstants.SUCCESS)
                .msg("获取审核记录成功")
                .build();
    }

    /**
     * 任务PO转VO（审核记录页使用）
     */
    private AuditTaskVO convertTaskToVO(AuditTaskPO po) {
        AuditTaskVO vo = mapStructMapper.auditTaskPoToVo(po);

        AuditNodePO node = auditNodeMapper.selectById(po.getAuditNodeId());
        if (node != null) {
            vo.setNodeName(node.getName());
            vo.setNodeType(node.getType());
        }

        AuditInstancePO instance = auditInstanceMapper.selectById(po.getAuditInstanceId());
        if (instance != null) {
            vo.setVendorId(instance.getVendorId());
            VendorPO vendor = vendorMapper.selectById(instance.getVendorId());
            if (vendor != null) {
                vo.setCompanyName(vendor.getCompanyName());
            }
            VendorAuditRecordPO record = vendorAuditRecordMapper.selectById(instance.getAuditRecordId());
            if (record != null) {
                vo.setRound(record.getRound());
            }
        }

        return vo;
    }

    /**
     * 资质审核（根据审核结果自动决定事件）
     */
    @Override
    public HttpResponseVO<String> qualificationAudit(Integer vendorId, QualificationAuditDTO dto) {
        AuditEvent event = (dto.getAuditResult() == AuditResultEnum.PASS)
                ? AuditEvent.QUALIFICATION_PASS
                : AuditEvent.QUALIFICATION_FAIL;
        return stateMachine.fire(vendorId, event, dto);
    }

    /**
     * 技术测试审核（根据测试结果自动决定事件）
     */
    @Override
    public HttpResponseVO<String> techTestAudit(Integer vendorId, TechTestAuditDTO dto) {
        AuditEvent event = (dto.getTestResult() == TestResultEnum.PASSED)
                ? AuditEvent.TECH_TEST_PASS
                : AuditEvent.TECH_TEST_FAIL;
        return stateMachine.fire(vendorId, event, dto);
    }

    /**
     * 性能测试审核（根据测试结果自动决定事件）
     */
    @Override
    public HttpResponseVO<String> performanceTest(Integer vendorId, PerformanceTestDTO dto) {
        AuditEvent event = (dto.getTestResult() == TestResultEnum.PASSED)
                ? AuditEvent.PERFORMANCE_PASS
                : AuditEvent.PERFORMANCE_FAIL;
        return stateMachine.fire(vendorId, event, dto);
    }

    /**
     * 最终审批（根据是否批准自动决定事件）
     */
    @Override
    public HttpResponseVO<String> finalApproval(Integer vendorId, FinalApprovalDTO dto) {
        AuditEvent event = dto.getApproved()
                ? AuditEvent.FINAL_APPROVE
                : AuditEvent.FINAL_REJECT;
        return stateMachine.fire(vendorId, event, dto);
    }

}

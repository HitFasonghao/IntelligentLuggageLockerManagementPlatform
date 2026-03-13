package org.example.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.audit.dto.SimpleAssignDTO;
import org.example.audit.enums.AuditNodeTypeEnum;
import org.example.audit.enums.AuditRecordResultEnum;
import org.example.audit.enums.AuditTaskPriorityEnum;
import org.example.audit.enums.AuditTaskStatusEnum;
import org.example.audit.enums.VendorStatusEnum;
import org.example.audit.mapper.*;
import org.example.audit.po.*;
import org.example.audit.service.AuditAssignmentService;
import org.example.audit.vo.AdminOptionVO;
import org.example.audit.vo.AssignmentVendorVO;
import org.example.auth.constants.HttpStatusConstants;
import org.example.auth.mapper.PlatformAdminMapper;
import org.example.auth.mapper.VendorUserMapper;
import org.example.auth.po.PlatformAdminPO;
import org.example.auth.po.VendorUserPO;
import org.example.auth.vo.HttpResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 审核任务分配服务实现
 * @author fasonghao
 */
@Service
public class AuditAssignmentServiceImpl implements AuditAssignmentService {

    @Autowired
    private VendorMapper vendorMapper;

    @Autowired
    private VendorAuditRecordMapper vendorAuditRecordMapper;

    @Autowired
    private AuditNodeMapper auditNodeMapper;

    @Autowired
    private AuditTaskMapper auditTaskMapper;

    @Autowired
    private PlatformAdminMapper platformAdminMapper;

    @Autowired
    private VendorUserMapper vendorUserMapper;

    /**
     * 获取待分配的厂商列表
     * 查找处于审核中（PENDING/TESTING）且当前活跃节点尚未分配审核员的厂商
     */
    @Override
    public HttpResponseVO<List<AssignmentVendorVO>> getPendingAssignments() {
        LambdaQueryWrapper<VendorPO> vendorWrapper = Wrappers.lambdaQuery();
        vendorWrapper.in(VendorPO::getStatus, VendorStatusEnum.PENDING, VendorStatusEnum.TESTING);
        vendorWrapper.orderByAsc(VendorPO::getSubmittedTime);
        List<VendorPO> vendors = vendorMapper.selectList(vendorWrapper);

        List<AssignmentVendorVO> result = new ArrayList<>();

        for (VendorPO vendor : vendors) {
            // 获取最新审核记录
            LambdaQueryWrapper<VendorAuditRecordPO> recordWrapper = Wrappers.lambdaQuery();
            recordWrapper.eq(VendorAuditRecordPO::getVendorId, vendor.getVendorId());
            recordWrapper.orderByDesc(VendorAuditRecordPO::getRound);
            recordWrapper.last("LIMIT 1");
            VendorAuditRecordPO record = vendorAuditRecordMapper.selectOne(recordWrapper);
            if (record == null) continue;

            // 查找当前活跃的任务（未完成且未分配审核员）
            LambdaQueryWrapper<AuditTaskPO> taskWrapper = Wrappers.lambdaQuery();
            taskWrapper.eq(AuditTaskPO::getAuditRecordId, record.getAuditRecordId());
            taskWrapper.ne(AuditTaskPO::getStatus, AuditTaskStatusEnum.COMPLETED);
            taskWrapper.isNull(AuditTaskPO::getAdminId);
            taskWrapper.last("LIMIT 1");
            AuditTaskPO activeTask = auditTaskMapper.selectOne(taskWrapper);
            if (activeTask == null) continue;

            AssignmentVendorVO vo = new AssignmentVendorVO();
            vo.setVendorId(vendor.getVendorId());
            vo.setCompanyName(vendor.getCompanyName());
            vo.setShortName(vendor.getShortName());
            vo.setContactPerson(vendor.getContactPerson());
            vo.setContactPhone(vendor.getContactPhone());
            vo.setStatus(vendor.getStatus());
            vo.setSubmittedTime(vendor.getSubmittedTime());
            vo.setCurrentRound(record.getRound());
            vo.setAuditTaskId(activeTask.getAuditTaskId());
            vo.setAuditNodeId(activeTask.getAuditNodeId());

            AuditNodePO node = auditNodeMapper.selectById(activeTask.getAuditNodeId());
            if (node != null) {
                vo.setCurrentNodeName(node.getName());
            }

            // 填充申请用户名称
            if (record.getVendorUserId() != null) {
                VendorUserPO vendorUser = vendorUserMapper.selectById(record.getVendorUserId());
                if (vendorUser != null) {
                    vo.setVendorUserName(vendorUser.getRealName() != null ? vendorUser.getRealName() : vendorUser.getUsername());
                }
            }

            result.add(vo);
        }

        return HttpResponseVO.<List<AssignmentVendorVO>>builder()
                .data(result)
                .code(HttpStatusConstants.SUCCESS)
                .msg("获取待分配列表成功")
                .build();
    }

    /**
     * 分配审核任务（更新已有任务的审核员）
     */
    @Override
    public HttpResponseVO<String> assign(SimpleAssignDTO dto) {
        VendorPO vendor = vendorMapper.selectById(dto.getVendorId());
        if (vendor == null) {
            return HttpResponseVO.<String>builder().code(HttpStatusConstants.ERROR).msg("厂商不存在").build();
        }

        // 获取最新审核记录
        LambdaQueryWrapper<VendorAuditRecordPO> recordWrapper = Wrappers.lambdaQuery();
        recordWrapper.eq(VendorAuditRecordPO::getVendorId, dto.getVendorId());
        recordWrapper.orderByDesc(VendorAuditRecordPO::getRound);
        recordWrapper.last("LIMIT 1");
        VendorAuditRecordPO record = vendorAuditRecordMapper.selectOne(recordWrapper);
        if (record == null) {
            return HttpResponseVO.<String>builder().code(HttpStatusConstants.ERROR).msg("未找到审核记录").build();
        }

        // 查找当前未分配审核员的活跃任务
        LambdaQueryWrapper<AuditTaskPO> taskWrapper = Wrappers.lambdaQuery();
        taskWrapper.eq(AuditTaskPO::getAuditRecordId, record.getAuditRecordId());
        taskWrapper.ne(AuditTaskPO::getStatus, AuditTaskStatusEnum.COMPLETED);
        taskWrapper.isNull(AuditTaskPO::getAdminId);
        taskWrapper.last("LIMIT 1");
        AuditTaskPO activeTask = auditTaskMapper.selectOne(taskWrapper);
        if (activeTask == null) {
            return HttpResponseVO.<String>builder().code(HttpStatusConstants.ERROR).msg("未找到待分配的审核节点").build();
        }

        // 更新任务分配信息
        activeTask.setAdminId(dto.getAdminId());
        activeTask.setPriority(dto.getPriority() != null ? dto.getPriority() : AuditTaskPriorityEnum.MEDIUM);
        activeTask.setDueDate(dto.getDueDate());
        auditTaskMapper.updateById(activeTask);

        // 更新审核记录的管理员和审核进度
        record.setAdminId(dto.getAdminId());
        AuditNodePO node = auditNodeMapper.selectById(activeTask.getAuditNodeId());
        if (node != null) {
            record.setResult(nodeTypeToResult(node.getType()));
        }
        vendorAuditRecordMapper.updateById(record);

        return HttpResponseVO.<String>builder()
                .code(HttpStatusConstants.SUCCESS)
                .msg("任务分配成功")
                .build();
    }

    @Override
    public HttpResponseVO<List<AdminOptionVO>> getAdminOptions() {
        LambdaQueryWrapper<PlatformAdminPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(PlatformAdminPO::getIsActive, true);
        List<PlatformAdminPO> admins = platformAdminMapper.selectList(wrapper);

        List<AdminOptionVO> options = admins.stream().map(admin -> {
            AdminOptionVO vo = new AdminOptionVO();
            vo.setAdminId(admin.getAdminId());
            vo.setUsername(admin.getUsername());
            vo.setRealName(admin.getRealName());
            return vo;
        }).collect(Collectors.toList());

        return HttpResponseVO.<List<AdminOptionVO>>builder()
                .data(options)
                .code(HttpStatusConstants.SUCCESS)
                .msg("获取管理员列表成功")
                .build();
    }

    /**
     * 根据节点类型返回对应的"待审核"结果状态
     */
    private AuditRecordResultEnum nodeTypeToResult(AuditNodeTypeEnum nodeType) {
        return switch (nodeType) {
            case QUALIFICATION -> AuditRecordResultEnum.PENDING_QUALIFICATION;
            case FUNCTIONAL_TEST -> AuditRecordResultEnum.PENDING_FUNCTIONAL_TEST;
            case PERFORMANCE -> AuditRecordResultEnum.PENDING_PERFORMANCE_TEST;
            case MANUAL_REVIEW -> AuditRecordResultEnum.PENDING_FINAL_APPROVAL;
            default -> AuditRecordResultEnum.NOT_STARTED;
        };
    }
}

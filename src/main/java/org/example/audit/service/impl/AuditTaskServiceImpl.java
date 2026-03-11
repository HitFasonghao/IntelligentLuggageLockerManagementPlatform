package org.example.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.audit.dto.AssignAuditTaskDTO;
import org.example.audit.dto.UpdateAuditTaskDTO;
import org.example.audit.enums.AuditTaskStatusEnum;
import org.example.audit.mapper.*;
import org.example.audit.po.*;
import org.example.audit.service.AuditTaskService;
import org.example.audit.vo.AuditTaskVO;
import org.example.auth.common.PcUserInfo;
import org.example.auth.common.UserContext;
import org.example.auth.constants.HttpStatusConstants;
import org.example.auth.mapper.PlatformAdminMapper;
import org.example.auth.po.PlatformAdminPO;
import org.example.auth.vo.HttpResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 审核任务服务实现
 * @author fasonghao
 */
@Service
public class AuditTaskServiceImpl implements AuditTaskService {

    @Autowired
    private AuditTaskMapper auditTaskMapper;

    @Autowired
    private AuditNodeMapper auditNodeMapper;

    @Autowired
    private VendorMapper vendorMapper;

    @Autowired
    private AuditMapStructMapper mapStructMapper;

    @Autowired
    private VendorAuditRecordMapper vendorAuditRecordMapper;

    @Autowired
    private PlatformAdminMapper platformAdminMapper;

    /**
     * 获取当前管理员的任务列表
     */
    @Override
    public HttpResponseVO<List<AuditTaskVO>> getMyTasks() {
        PcUserInfo userInfo = UserContext.get();

        LambdaQueryWrapper<AuditTaskPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AuditTaskPO::getAdminId, userInfo.getUserId());
        wrapper.ne(AuditTaskPO::getStatus, AuditTaskStatusEnum.COMPLETED);
        wrapper.orderByAsc(AuditTaskPO::getStatus);
        wrapper.orderByDesc(AuditTaskPO::getPriority);
        wrapper.orderByAsc(AuditTaskPO::getDueDate);
        List<AuditTaskPO> tasks = auditTaskMapper.selectList(wrapper);

        List<AuditTaskVO> voList = tasks.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return HttpResponseVO.<List<AuditTaskVO>>builder()
                .data(voList)
                .code(HttpStatusConstants.SUCCESS)
                .msg("获取任务列表成功")
                .build();
    }

    /**
     * 获取指定厂商的任务列表
     */
    @Override
    public HttpResponseVO<List<AuditTaskVO>> getTasksByVendor(Integer vendorId) {
        LambdaQueryWrapper<AuditTaskPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AuditTaskPO::getVendorId, vendorId);
        wrapper.orderByAsc(AuditTaskPO::getDueDate);
        List<AuditTaskPO> tasks = auditTaskMapper.selectList(wrapper);

        List<AuditTaskVO> voList = tasks.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return HttpResponseVO.<List<AuditTaskVO>>builder()
                .data(voList)
                .code(HttpStatusConstants.SUCCESS)
                .msg("获取任务列表成功")
                .build();
    }

    /**
     * 分配审核任务（更新已有任务的审核员）
     */
    @Override
    public HttpResponseVO<String> assignTask(AssignAuditTaskDTO dto) {
        AuditTaskPO task = auditTaskMapper.selectById(dto.getAuditTaskId());
        if (task == null) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("任务不存在")
                    .build();
        }

        task.setAdminId(dto.getAdminId());
        task.setPriority(dto.getPriority() != null ? dto.getPriority() : org.example.audit.enums.AuditTaskPriorityEnum.MEDIUM);
        task.setDueDate(dto.getDueDate());
        auditTaskMapper.updateById(task);

        return HttpResponseVO.<String>builder()
                .code(HttpStatusConstants.SUCCESS)
                .msg("任务分配成功")
                .build();
    }

    /**
     * 更新任务状态/备注
     */
    @Override
    public HttpResponseVO<String> updateTask(Integer taskId, UpdateAuditTaskDTO dto) {
        AuditTaskPO existing = auditTaskMapper.selectById(taskId);
        if (existing == null) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("任务不存在")
                    .build();
        }

        LambdaUpdateWrapper<AuditTaskPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AuditTaskPO::getAuditTaskId, taskId);

        if (dto.getStatus() != null) {
            wrapper.set(AuditTaskPO::getStatus, dto.getStatus());
            // 完成时自动记录完成时间
            if (dto.getStatus() == AuditTaskStatusEnum.COMPLETED) {
                wrapper.set(AuditTaskPO::getCompletedTime, LocalDateTime.now());
            }
        }
        if (dto.getPriority() != null) {
            wrapper.set(AuditTaskPO::getPriority, dto.getPriority());
        }
        if (dto.getNotes() != null) {
            wrapper.set(AuditTaskPO::getNotes, dto.getNotes());
        }

        int result = auditTaskMapper.update(null, wrapper);
        if (result > 0) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.SUCCESS)
                    .msg("任务更新成功")
                    .build();
        }
        return HttpResponseVO.<String>builder()
                .code(HttpStatusConstants.ERROR)
                .msg("任务更新失败")
                .build();
    }

    /**
     * 获取单个任务详情
     */
    @Override
    public HttpResponseVO<AuditTaskVO> getTaskById(Integer taskId) {
        AuditTaskPO task = auditTaskMapper.selectById(taskId);
        if (task == null) {
            return HttpResponseVO.<AuditTaskVO>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("任务不存在")
                    .build();
        }
        return HttpResponseVO.<AuditTaskVO>builder()
                .data(convertToVO(task))
                .code(HttpStatusConstants.SUCCESS)
                .msg("获取任务详情成功")
                .build();
    }

    /**
     * PO转VO，补充关联信息
     */
    private AuditTaskVO convertToVO(AuditTaskPO po) {
        AuditTaskVO vo = mapStructMapper.auditTaskPoToVo(po);

        // 补充节点名称和类型
        AuditNodePO node = auditNodeMapper.selectById(po.getAuditNodeId());
        if (node != null) {
            vo.setNodeName(node.getName());
            vo.setNodeType(node.getType());
        }

        // 补充厂商信息
        vo.setVendorId(po.getVendorId());
        VendorPO vendor = vendorMapper.selectById(po.getVendorId());
        if (vendor != null) {
            vo.setCompanyName(vendor.getCompanyName());
        }
        // 补充轮次
        VendorAuditRecordPO record = vendorAuditRecordMapper.selectById(po.getAuditRecordId());
        if (record != null) {
            vo.setRound(record.getRound());
        }
        // 补充审核管理员名称
        if (po.getAdminId() != null) {
            PlatformAdminPO admin = platformAdminMapper.selectById(po.getAdminId());
            if (admin != null) {
                vo.setAdminName(admin.getRealName() != null ? admin.getRealName() : admin.getUsername());
            }
        }

        return vo;
    }

    /**
     * 根据审核记录ID获取任务列表
     */
    @Override
    public HttpResponseVO<List<AuditTaskVO>> getTasksByRecord(Integer recordId) {
        LambdaQueryWrapper<AuditTaskPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AuditTaskPO::getAuditRecordId, recordId);
        wrapper.orderByAsc(AuditTaskPO::getAuditNodeId);
        List<AuditTaskPO> tasks = auditTaskMapper.selectList(wrapper);

        List<AuditTaskVO> voList = tasks.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return HttpResponseVO.<List<AuditTaskVO>>builder()
                .data(voList)
                .code(HttpStatusConstants.SUCCESS)
                .msg("获取任务列表成功")
                .build();
    }
}

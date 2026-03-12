package org.example.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.audit.dto.AssignAuditTaskDTO;
import org.example.audit.dto.AuditTaskQueryDTO;
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

import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * 获取当前管理员的任务列表（支持条件查询 + 分页）
     */
    @Override
    public HttpResponseVO<Map<String, Object>> getMyTasks(AuditTaskQueryDTO queryDTO) {
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

        // 内存过滤
        List<AuditTaskVO> filteredList = voList.stream()
                .filter(vo -> !StringUtils.hasText(queryDTO.getCompanyName()) ||
                        (vo.getCompanyName() != null && vo.getCompanyName().toLowerCase().contains(queryDTO.getCompanyName().toLowerCase())))
                .filter(vo -> queryDTO.getAuditNodeId() == null ||
                        vo.getAuditNodeId().equals(queryDTO.getAuditNodeId()))
                .filter(vo -> queryDTO.getPriority() == null ||
                        vo.getPriority() == queryDTO.getPriority())
                .filter(vo -> queryDTO.getDueDateStart() == null ||
                        (vo.getDueDate() != null && !vo.getDueDate().isBefore(queryDTO.getDueDateStart())))
                .filter(vo -> queryDTO.getDueDateEnd() == null ||
                        (vo.getDueDate() != null && !vo.getDueDate().isAfter(queryDTO.getDueDateEnd())))
                .collect(Collectors.toList());

        // 内存分页
        int total = filteredList.size();
        int pageNum = queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize();
        int fromIndex = Math.min((pageNum - 1) * pageSize, total);
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<AuditTaskVO> pagedList = filteredList.subList(fromIndex, toIndex);

        Map<String, Object> result = new HashMap<>();
        result.put("list", pagedList);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);

        return HttpResponseVO.<Map<String, Object>>builder()
                .data(result)
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
     * 获取审核节点选项列表
     */
    @Override
    public HttpResponseVO<List<Map<String, Object>>> getNodeOptions() {
        LambdaQueryWrapper<AuditNodePO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AuditNodePO::getIsActive, true);
        wrapper.orderByAsc(AuditNodePO::getOrder);
        List<AuditNodePO> nodes = auditNodeMapper.selectList(wrapper);

        List<Map<String, Object>> options = nodes.stream().map(node -> {
            Map<String, Object> option = new HashMap<>();
            option.put("auditNodeId", node.getAuditNodeId());
            option.put("name", node.getName());
            return option;
        }).collect(Collectors.toList());

        return HttpResponseVO.<List<Map<String, Object>>>builder()
                .data(options)
                .code(HttpStatusConstants.SUCCESS)
                .msg("获取审核节点选项成功")
                .build();
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

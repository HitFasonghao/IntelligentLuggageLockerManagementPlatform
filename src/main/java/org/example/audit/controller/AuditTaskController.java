package org.example.audit.controller;

import jakarta.validation.Valid;
import org.example.audit.dto.AssignAuditTaskDTO;
import org.example.audit.dto.AuditTaskQueryDTO;
import org.example.audit.dto.UpdateAuditTaskDTO;
import org.example.audit.service.AuditTaskService;
import org.example.audit.vo.AuditTaskVO;
import org.example.auth.vo.HttpResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 审核任务工作台接口（平台管理员使用）
 * @author fasonghao
 */
@RestController
@RequestMapping("/audit/tasks")
public class AuditTaskController {

    @Autowired
    private AuditTaskService auditTaskService;

    /**
     * 获取当前管理员的任务列表
     */
    @GetMapping("/my")
    public HttpResponseVO<Map<String, Object>> getMyTasks(AuditTaskQueryDTO queryDTO) {
        return auditTaskService.getMyTasks(queryDTO);
    }

    /**
     * 获取审核节点选项列表（用于下拉选择）
     */
    @GetMapping("/nodeOptions")
    public HttpResponseVO<List<Map<String, Object>>> getNodeOptions() {
        return auditTaskService.getNodeOptions();
    }

    /**
     * 获取单个任务详情
     */
    @GetMapping("/{taskId}")
    public HttpResponseVO<AuditTaskVO> getTaskById(@PathVariable Integer taskId) {
        return auditTaskService.getTaskById(taskId);
    }

    /**
     * 获取指定厂商的任务列表
     */
    @GetMapping("/vendor/{vendorId}")
    public HttpResponseVO<List<AuditTaskVO>> getTasksByVendor(@PathVariable Integer vendorId) {
        return auditTaskService.getTasksByVendor(vendorId);
    }

    /**
     * 根据审核记录ID获取任务列表
     */
    @GetMapping("/record/{recordId}")
    public HttpResponseVO<List<AuditTaskVO>> getTasksByRecord(@PathVariable Integer recordId) {
        return auditTaskService.getTasksByRecord(recordId);
    }

    /**
     * 分配审核任务
     */
    @PostMapping
    public HttpResponseVO<String> assignTask(@RequestBody @Valid AssignAuditTaskDTO dto) {
        return auditTaskService.assignTask(dto);
    }

    /**
     * 更新任务状态/备注
     */
    @PatchMapping("/{taskId}")
    public HttpResponseVO<String> updateTask(@PathVariable Integer taskId,
                                             @RequestBody @Valid UpdateAuditTaskDTO dto) {
        return auditTaskService.updateTask(taskId, dto);
    }
}

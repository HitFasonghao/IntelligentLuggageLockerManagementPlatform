package org.example.audit.controller;

import jakarta.validation.Valid;
import org.example.audit.dto.AssignAuditTaskDTO;
import org.example.audit.dto.UpdateAuditTaskDTO;
import org.example.audit.service.AuditTaskService;
import org.example.audit.vo.AuditTaskVO;
import org.example.auth.vo.HttpResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public HttpResponseVO<List<AuditTaskVO>> getMyTasks() {
        return auditTaskService.getMyTasks();
    }

    /**
     * 获取指定厂商的任务列表
     */
    @GetMapping("/vendor/{vendorId}")
    public HttpResponseVO<List<AuditTaskVO>> getTasksByVendor(@PathVariable Integer vendorId) {
        return auditTaskService.getTasksByVendor(vendorId);
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

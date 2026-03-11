package org.example.audit.controller;

import jakarta.validation.Valid;
import org.example.audit.dto.SimpleAssignDTO;
import org.example.audit.service.AuditAssignmentService;
import org.example.audit.vo.AdminOptionVO;
import org.example.audit.vo.AssignmentVendorVO;
import org.example.auth.vo.HttpResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审核任务分配接口（超级管理员使用）
 * @author fasonghao
 */
@RestController
@RequestMapping("/audit/assignments")
public class AuditAssignmentController {

    @Autowired
    private AuditAssignmentService auditAssignmentService;

    /**
     * 获取待分配的厂商列表
     */
    @GetMapping
    public HttpResponseVO<List<AssignmentVendorVO>> getPendingAssignments() {
        return auditAssignmentService.getPendingAssignments();
    }

    /**
     * 分配审核任务
     */
    @PostMapping
    public HttpResponseVO<String> assign(@RequestBody @Valid SimpleAssignDTO dto) {
        return auditAssignmentService.assign(dto);
    }

    /**
     * 获取可分配的管理员列表
     */
    @GetMapping("/admins")
    public HttpResponseVO<List<AdminOptionVO>> getAdminOptions() {
        return auditAssignmentService.getAdminOptions();
    }
}

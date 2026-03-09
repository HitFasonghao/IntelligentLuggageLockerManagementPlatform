package org.example.audit.controller;

import jakarta.validation.Valid;
import org.example.audit.dto.CreateAuditNodeDTO;
import org.example.audit.dto.UpdateAuditNodeDTO;
import org.example.audit.service.AuditNodeService;
import org.example.audit.vo.AuditNodeVO;
import org.example.auth.vo.HttpResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审核流程节点配置接口（超级管理员使用）
 * @author fasonghao
 */
@RestController
@RequestMapping("/audit/nodes")
public class AuditNodeController {

    @Autowired
    private AuditNodeService auditNodeService;

    /**
     * 获取所有审核节点
     */
    @GetMapping
    public HttpResponseVO<List<AuditNodeVO>> getAllNodes() {
        return auditNodeService.getAllNodes();
    }

    /**
     * 新增审核节点
     */
    @PostMapping
    public HttpResponseVO<String> addNode(@RequestBody @Valid CreateAuditNodeDTO dto) {
        return auditNodeService.addNode(dto);
    }

    /**
     * 更新审核节点
     */
    @PatchMapping("/{nodeId}")
    public HttpResponseVO<String> updateNode(@PathVariable Integer nodeId,
                                             @RequestBody @Valid UpdateAuditNodeDTO dto) {
        return auditNodeService.updateNode(nodeId, dto);
    }

    /**
     * 删除审核节点
     */
    @DeleteMapping("/{nodeId}")
    public HttpResponseVO<String> deleteNode(@PathVariable Integer nodeId) {
        return auditNodeService.deleteNode(nodeId);
    }
}

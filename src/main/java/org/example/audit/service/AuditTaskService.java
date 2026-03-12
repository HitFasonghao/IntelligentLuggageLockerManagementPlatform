package org.example.audit.service;

import org.example.audit.dto.AssignAuditTaskDTO;
import org.example.audit.dto.AuditTaskQueryDTO;
import org.example.audit.dto.UpdateAuditTaskDTO;
import org.example.audit.vo.AuditTaskVO;
import org.example.auth.vo.HttpResponseVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 审核任务服务接口
 * @author fasonghao
 */
@Service
public interface AuditTaskService {

    /** 获取当前管理员的任务列表 */
    HttpResponseVO<Map<String, Object>> getMyTasks(AuditTaskQueryDTO queryDTO);

    /** 获取审核节点选项列表 */
    HttpResponseVO<List<Map<String, Object>>> getNodeOptions();

    /** 获取指定厂商的任务列表 */
    HttpResponseVO<List<AuditTaskVO>> getTasksByVendor(Integer vendorId);

    /** 分配审核任务 */
    HttpResponseVO<String> assignTask(AssignAuditTaskDTO dto);

    /** 更新任务状态/备注 */
    HttpResponseVO<String> updateTask(Integer taskId, UpdateAuditTaskDTO dto);

    /** 获取单个任务详情 */
    HttpResponseVO<AuditTaskVO> getTaskById(Integer taskId);

    /** 根据审核记录ID获取任务列表 */
    HttpResponseVO<List<AuditTaskVO>> getTasksByRecord(Integer recordId);
}

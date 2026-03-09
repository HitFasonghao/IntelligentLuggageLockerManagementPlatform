package org.example.audit.service;

import org.example.audit.dto.AssignAuditTaskDTO;
import org.example.audit.dto.UpdateAuditTaskDTO;
import org.example.audit.vo.AuditTaskVO;
import org.example.auth.vo.HttpResponseVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 审核任务服务接口
 * @author fasonghao
 */
@Service
public interface AuditTaskService {

    /** 获取当前管理员的任务列表 */
    HttpResponseVO<List<AuditTaskVO>> getMyTasks();

    /** 获取指定厂商的任务列表 */
    HttpResponseVO<List<AuditTaskVO>> getTasksByVendor(Integer vendorId);

    /** 分配审核任务 */
    HttpResponseVO<String> assignTask(AssignAuditTaskDTO dto);

    /** 更新任务状态/备注 */
    HttpResponseVO<String> updateTask(Integer taskId, UpdateAuditTaskDTO dto);
}

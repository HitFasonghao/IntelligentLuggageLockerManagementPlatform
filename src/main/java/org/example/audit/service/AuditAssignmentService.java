package org.example.audit.service;

import org.example.audit.dto.SimpleAssignDTO;
import org.example.audit.vo.AdminOptionVO;
import org.example.audit.vo.AssignmentVendorVO;
import org.example.auth.vo.HttpResponseVO;

import java.util.List;

/**
 * 审核任务分配服务接口
 * @author fasonghao
 */
public interface AuditAssignmentService {

    /** 获取待分配的厂商列表 */
    HttpResponseVO<List<AssignmentVendorVO>> getPendingAssignments();

    /** 分配审核任务（简化入口） */
    HttpResponseVO<String> assign(SimpleAssignDTO dto);

    /** 获取可分配的管理员列表 */
    HttpResponseVO<List<AdminOptionVO>> getAdminOptions();
}

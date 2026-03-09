package org.example.audit.service;

import org.example.audit.dto.CreateAuditNodeDTO;
import org.example.audit.dto.UpdateAuditNodeDTO;
import org.example.audit.vo.AuditNodeVO;
import org.example.auth.vo.HttpResponseVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 审核流程节点服务接口
 * @author fasonghao
 */
@Service
public interface AuditNodeService {

    /** 获取所有审核节点（按order排序） */
    HttpResponseVO<List<AuditNodeVO>> getAllNodes();

    /** 新增审核节点 */
    HttpResponseVO<String> addNode(CreateAuditNodeDTO dto);

    /** 更新审核节点 */
    HttpResponseVO<String> updateNode(Integer nodeId, UpdateAuditNodeDTO dto);

    /** 删除审核节点 */
    HttpResponseVO<String> deleteNode(Integer nodeId);
}

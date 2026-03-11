package org.example.audit.statemachine;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.audit.enums.AuditNodeTypeEnum;
import org.example.audit.enums.AuditRecordResultEnum;
import org.example.audit.enums.AuditTaskPriorityEnum;
import org.example.audit.enums.AuditTaskStatusEnum;
import org.example.audit.enums.AuditTypeEnum;
import org.example.audit.mapper.*;
import org.example.audit.po.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审核节点任务操作工具，供各动作复用
 * @author fasonghao
 */
@Component
public class AuditNodeHelper {

    @Autowired
    private AuditNodeMapper auditNodeMapper;

    @Autowired
    private AuditTaskMapper auditTaskMapper;

    @Autowired
    private VendorAuditRecordMapper vendorAuditRecordMapper;

    /**
     * 完成指定类型的节点任务（设置 completedTime、passed、notes）
     */
    public void completeNode(Integer auditRecordId, AuditNodeTypeEnum nodeType,
                              boolean passed, String notes) {
        AuditNodePO node = findActiveNode(nodeType);
        if (node == null) return;

        LambdaUpdateWrapper<AuditTaskPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AuditTaskPO::getAuditRecordId, auditRecordId);
        wrapper.eq(AuditTaskPO::getAuditNodeId, node.getAuditNodeId());
        wrapper.ne(AuditTaskPO::getStatus, AuditTaskStatusEnum.COMPLETED);
        wrapper.set(AuditTaskPO::getStatus, AuditTaskStatusEnum.COMPLETED);
        wrapper.set(AuditTaskPO::getCompletedTime, LocalDateTime.now());
        wrapper.set(AuditTaskPO::getPassed, passed);
        if (notes != null) {
            wrapper.set(AuditTaskPO::getNotes, notes);
        }
        auditTaskMapper.update(null, wrapper);
    }

    /**
     * 创建审核记录，并只为第一个启用的节点创建任务
     */
    public VendorAuditRecordPO createRecordWithFirstTask(Integer vendorId, int round,
                                                          AuditTypeEnum type, Object snapshot) {
        VendorAuditRecordPO record = new VendorAuditRecordPO();
        record.setVendorId(vendorId);
        record.setRound(round);
        record.setType(type);
        record.setData(snapshot);
        record.setAdminId(0);
        record.setResult(AuditRecordResultEnum.NOT_STARTED);
        vendorAuditRecordMapper.insert(record);

        // 获取第一个启用的审核节点
        LambdaQueryWrapper<AuditNodePO> nodeWrapper = Wrappers.lambdaQuery();
        nodeWrapper.eq(AuditNodePO::getIsActive, true);
        nodeWrapper.orderByAsc(AuditNodePO::getOrder);
        nodeWrapper.last("LIMIT 1");
        AuditNodePO firstNode = auditNodeMapper.selectOne(nodeWrapper);

        if (firstNode != null) {
            AuditTaskPO task = new AuditTaskPO();
            task.setVendorId(vendorId);
            task.setAuditRecordId(record.getAuditRecordId());
            task.setAuditNodeId(firstNode.getAuditNodeId());
            task.setStatus(AuditTaskStatusEnum.PENDING);
            auditTaskMapper.insert(task);
        }

        return record;
    }

    /**
     * 为指定节点类型创建新任务（上一个节点完成后调用）
     */
    public void createNextTask(Integer vendorId, Integer auditRecordId, AuditNodeTypeEnum nodeType) {
        AuditNodePO node = findActiveNode(nodeType);
        if (node == null) return;

        AuditTaskPO task = new AuditTaskPO();
        task.setVendorId(vendorId);
        task.setAuditRecordId(auditRecordId);
        task.setAuditNodeId(node.getAuditNodeId());
        task.setStatus(AuditTaskStatusEnum.PENDING);
        auditTaskMapper.insert(task);
    }

    /**
     * 获取厂商最新的审核记录
     */
    public VendorAuditRecordPO getLatestRecord(Integer vendorId) {
        LambdaQueryWrapper<VendorAuditRecordPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(VendorAuditRecordPO::getVendorId, vendorId);
        wrapper.orderByDesc(VendorAuditRecordPO::getRound);
        wrapper.last("LIMIT 1");
        return vendorAuditRecordMapper.selectOne(wrapper);
    }

    /**
     * 为指定节点任务自动分配审核员
     */
    public void autoAssignTask(Integer auditRecordId, AuditNodeTypeEnum nodeType, Integer adminId) {
        AuditNodePO node = findActiveNode(nodeType);
        if (node == null) return;

        LambdaUpdateWrapper<AuditTaskPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AuditTaskPO::getAuditRecordId, auditRecordId);
        wrapper.eq(AuditTaskPO::getAuditNodeId, node.getAuditNodeId());
        wrapper.set(AuditTaskPO::getAdminId, adminId);
        wrapper.set(AuditTaskPO::getPriority, AuditTaskPriorityEnum.MEDIUM);
        wrapper.set(AuditTaskPO::getDueDate, LocalDateTime.now().plusHours(node.getTimeoutHours()));
        auditTaskMapper.update(null, wrapper);
    }

    /**
     * 查找指定审核记录中某个节点的已分配管理员ID
     */
    public Integer findAssignedAdminId(Integer auditRecordId, AuditNodeTypeEnum nodeType) {
        AuditNodePO node = findActiveNode(nodeType);
        if (node == null) return null;

        LambdaQueryWrapper<AuditTaskPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AuditTaskPO::getAuditRecordId, auditRecordId);
        wrapper.eq(AuditTaskPO::getAuditNodeId, node.getAuditNodeId());
        wrapper.last("LIMIT 1");
        AuditTaskPO task = auditTaskMapper.selectOne(wrapper);
        return task != null ? task.getAdminId() : null;
    }

    /**
     * 查找启用状态的指定类型节点
     */
    private AuditNodePO findActiveNode(AuditNodeTypeEnum nodeType) {
        LambdaQueryWrapper<AuditNodePO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AuditNodePO::getType, nodeType);
        wrapper.eq(AuditNodePO::getIsActive, true);
        return auditNodeMapper.selectOne(wrapper);
    }
}

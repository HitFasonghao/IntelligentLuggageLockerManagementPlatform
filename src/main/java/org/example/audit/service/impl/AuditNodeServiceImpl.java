package org.example.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.audit.dto.CreateAuditNodeDTO;
import org.example.audit.dto.UpdateAuditNodeDTO;
import org.example.audit.mapper.AuditMapStructMapper;
import org.example.audit.mapper.AuditNodeMapper;
import org.example.audit.po.AuditNodePO;
import org.example.audit.service.AuditNodeService;
import org.example.audit.vo.AuditNodeVO;
import org.example.auth.constants.HttpStatusConstants;
import org.example.auth.vo.HttpResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 审核流程节点服务实现
 * @author fasonghao
 */
@Service
public class AuditNodeServiceImpl implements AuditNodeService {

    @Autowired
    private AuditNodeMapper auditNodeMapper;

    @Autowired
    private AuditMapStructMapper mapStructMapper;

    @Override
    public HttpResponseVO<List<AuditNodeVO>> getAllNodes() {
        LambdaQueryWrapper<AuditNodePO> wrapper = Wrappers.lambdaQuery();
        wrapper.orderByAsc(AuditNodePO::getOrder);
        List<AuditNodePO> nodes = auditNodeMapper.selectList(wrapper);

        List<AuditNodeVO> voList = nodes.stream()
                .map(mapStructMapper::auditNodePoToVo)
                .collect(Collectors.toList());

        return HttpResponseVO.<List<AuditNodeVO>>builder()
                .data(voList)
                .code(HttpStatusConstants.SUCCESS)
                .msg("获取审核节点列表成功")
                .build();
    }

    @Override
    public HttpResponseVO<String> addNode(CreateAuditNodeDTO dto) {
        AuditNodePO po = new AuditNodePO();
        po.setName(dto.getName());
        po.setType(dto.getType());
        po.setOrder(dto.getOrder());
        po.setAutoPass(dto.getAutoPass() != null ? dto.getAutoPass() : false);
        po.setTimeoutHours(dto.getTimeoutHours() != null ? dto.getTimeoutHours() : 24);
        po.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        int result = auditNodeMapper.insert(po);
        if (result > 0) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.SUCCESS)
                    .msg("新增审核节点成功")
                    .build();
        }
        return HttpResponseVO.<String>builder()
                .code(HttpStatusConstants.ERROR)
                .msg("新增审核节点失败")
                .build();
    }

    @Override
    public HttpResponseVO<String> updateNode(Integer nodeId, UpdateAuditNodeDTO dto) {
        AuditNodePO existing = auditNodeMapper.selectById(nodeId);
        if (existing == null) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("审核节点不存在")
                    .build();
        }

        LambdaUpdateWrapper<AuditNodePO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AuditNodePO::getAuditNodeId, nodeId);

        if (dto.getName() != null) {
            wrapper.set(AuditNodePO::getName, dto.getName());
        }
        if (dto.getType() != null) {
            wrapper.set(AuditNodePO::getType, dto.getType());
        }
        if (dto.getOrder() != null) {
            wrapper.set(AuditNodePO::getOrder, dto.getOrder());
        }
        if (dto.getAutoPass() != null) {
            wrapper.set(AuditNodePO::getAutoPass, dto.getAutoPass());
        }
        if (dto.getTimeoutHours() != null) {
            wrapper.set(AuditNodePO::getTimeoutHours, dto.getTimeoutHours());
        }
        if (dto.getIsActive() != null) {
            wrapper.set(AuditNodePO::getIsActive, dto.getIsActive());
        }

        int result = auditNodeMapper.update(null, wrapper);
        if (result > 0) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.SUCCESS)
                    .msg("更新审核节点成功")
                    .build();
        }
        return HttpResponseVO.<String>builder()
                .code(HttpStatusConstants.ERROR)
                .msg("更新审核节点失败")
                .build();
    }

    @Override
    public HttpResponseVO<String> deleteNode(Integer nodeId) {
        AuditNodePO existing = auditNodeMapper.selectById(nodeId);
        if (existing == null) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("审核节点不存在")
                    .build();
        }

        auditNodeMapper.deleteById(nodeId);
        return HttpResponseVO.<String>builder()
                .code(HttpStatusConstants.SUCCESS)
                .msg("删除审核节点成功")
                .build();
    }
}

package org.example.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.audit.enums.AuditRecordResultEnum;
import org.example.audit.enums.VendorStatusEnum;
import org.example.audit.dto.SubmitVendorDTO;
import org.example.audit.dto.VendorProgressQueryDTO;
import org.example.audit.mapper.*;
import org.example.audit.po.*;
import org.example.audit.service.VendorService;
import org.example.audit.statemachine.AuditEvent;
import org.example.audit.statemachine.AuditStateMachine;
import org.example.audit.vo.*;
import org.example.auth.common.PcUserInfo;
import org.example.auth.common.UserContext;
import org.example.auth.constants.HttpStatusConstants;
import org.example.auth.enums.PcUserIdentityEnum;
import org.example.auth.mapper.PlatformAdminMapper;
import org.example.auth.po.PlatformAdminPO;
import org.example.auth.vo.HttpResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 厂商服务实现（厂商用户端）
 * <p>提交/重新提交等审核流程操作统一委托给 {@link AuditStateMachine} 处理
 *
 * @author fasonghao
 */
@Service
public class VendorServiceImpl implements VendorService {

    @Autowired
    private VendorMapper vendorMapper;

    @Autowired
    private VendorUserRelationMapper vendorUserRelationMapper;

    @Autowired
    private VendorAuditRecordMapper vendorAuditRecordMapper;

    @Autowired
    private AuditTaskMapper auditTaskMapper;

    @Autowired
    private AuditNodeMapper auditNodeMapper;

    @Autowired
    private AuditMapStructMapper mapStructMapper;

    @Autowired
    private PlatformAdminMapper platformAdminMapper;

    @Autowired
    private AuditStateMachine stateMachine;

    /**
     * 保存草稿
     */
    @Override
    @Transactional
    public HttpResponseVO<Integer> saveDraft(SubmitVendorDTO dto) {
        PcUserInfo userInfo = UserContext.get();

        if (dto.getVendorId() != null) {
            // 编辑已有草稿
            VendorPO vendor = vendorMapper.selectById(dto.getVendorId());
            if (vendor == null) {
                return HttpResponseVO.<Integer>builder().code(HttpStatusConstants.ERROR).msg("厂商不存在").build();
            }
            if (vendor.getStatus() != VendorStatusEnum.DRAFT && vendor.getStatus() != VendorStatusEnum.REJECTED) {
                return HttpResponseVO.<Integer>builder().code(HttpStatusConstants.ERROR).msg("当前状态不允许编辑").build();
            }
            if (!isVendorUser(userInfo.getUserId(), dto.getVendorId())) {
                return HttpResponseVO.<Integer>builder().code(HttpStatusConstants.ERROR).msg("无权操作该厂商").build();
            }
            copyDtoToVendor(dto, vendor);
            vendor.setStatus(VendorStatusEnum.DRAFT);
            vendorMapper.updateById(vendor);
            return HttpResponseVO.<Integer>builder().data(vendor.getVendorId()).code(HttpStatusConstants.SUCCESS).msg("草稿保存成功").build();
        }

        // 新建草稿
        VendorPO vendor = new VendorPO();
        copyDtoToVendor(dto, vendor);
        vendor.setStatus(VendorStatusEnum.DRAFT);
        vendorMapper.insert(vendor);

        // 创建关联关系
        VendorUserRelationPO relation = new VendorUserRelationPO();
        relation.setVendorUserId(userInfo.getUserId());
        relation.setVendorId(vendor.getVendorId());
        relation.setIsMain(true);
        vendorUserRelationMapper.insert(relation);

        return HttpResponseVO.<Integer>builder().data(vendor.getVendorId()).code(HttpStatusConstants.SUCCESS).msg("草稿保存成功").build();
    }

    /**
     * 提交入驻申请（委托状态机处理流程流转）
     */
    @Override
    @Transactional
    public HttpResponseVO<Integer> submitApplication(SubmitVendorDTO dto) {
        PcUserInfo userInfo = UserContext.get();

        if (dto.getVendorId() != null) {
            // 提交已有草稿
            VendorPO vendor = vendorMapper.selectById(dto.getVendorId());
            if (vendor == null) {
                return HttpResponseVO.<Integer>builder().code(HttpStatusConstants.ERROR).msg("厂商不存在").build();
            }
            if (vendor.getStatus() != VendorStatusEnum.DRAFT) {
                return HttpResponseVO.<Integer>builder().code(HttpStatusConstants.ERROR).msg("当前状态不允许提交").build();
            }
            if (!isVendorUser(userInfo.getUserId(), dto.getVendorId())) {
                return HttpResponseVO.<Integer>builder().code(HttpStatusConstants.ERROR).msg("无权操作该厂商").build();
            }
            copyDtoToVendor(dto, vendor);
            vendorMapper.updateById(vendor);

            // 委托状态机处理流程流转
            HttpResponseVO<String> result = stateMachine.fire(vendor.getVendorId(), AuditEvent.SUBMIT, dto);
            return HttpResponseVO.<Integer>builder()
                    .data(vendor.getVendorId())
                    .code(result.getCode())
                    .msg(result.getMsg())
                    .build();
        }

        // 直接新建并提交
        VendorPO vendor = new VendorPO();
        copyDtoToVendor(dto, vendor);
        vendor.setStatus(VendorStatusEnum.DRAFT);
        vendorMapper.insert(vendor);

        VendorUserRelationPO relation = new VendorUserRelationPO();
        relation.setVendorUserId(userInfo.getUserId());
        relation.setVendorId(vendor.getVendorId());
        relation.setIsMain(true);
        vendorUserRelationMapper.insert(relation);

        // 委托状态机处理流程流转
        HttpResponseVO<String> result = stateMachine.fire(vendor.getVendorId(), AuditEvent.SUBMIT, dto);
        return HttpResponseVO.<Integer>builder()
                .data(vendor.getVendorId())
                .code(result.getCode())
                .msg(result.getMsg())
                .build();
    }

    /**
     * 驳回后重新提交（委托状态机处理流程流转）
     */
    @Override
    @Transactional
    public HttpResponseVO<String> resubmit(Integer vendorId, SubmitVendorDTO dto) {
        PcUserInfo userInfo = UserContext.get();
        if (userInfo.getRole() != PcUserIdentityEnum.VENDOR_USER) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("仅厂商用户可操作")
                    .build();
        }

        VendorPO vendor = vendorMapper.selectById(vendorId);
        if (vendor == null) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("厂商不存在")
                    .build();
        }

        if (!isVendorUser(userInfo.getUserId(), vendorId)) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("无权操作该厂商")
                    .build();
        }

        // 更新厂商信息
        copyDtoToVendor(dto, vendor);
        vendorMapper.updateById(vendor);

        // 委托状态机处理流程流转（状态校验由状态机完成）
        return stateMachine.fire(vendorId, AuditEvent.RESUBMIT, dto);
    }

    /**
     * 删除草稿
     */
    @Override
    @Transactional
    public HttpResponseVO<String> deleteDraft(Integer vendorId) {
        PcUserInfo userInfo = UserContext.get();

        VendorPO vendor = vendorMapper.selectById(vendorId);
        if (vendor == null) {
            return HttpResponseVO.<String>builder().code(HttpStatusConstants.ERROR).msg("厂商不存在").build();
        }
        if (vendor.getStatus() != VendorStatusEnum.DRAFT) {
            return HttpResponseVO.<String>builder().code(HttpStatusConstants.ERROR).msg("仅草稿状态可删除").build();
        }
        if (!isVendorUser(userInfo.getUserId(), vendorId)) {
            return HttpResponseVO.<String>builder().code(HttpStatusConstants.ERROR).msg("无权操作该厂商").build();
        }

        // 删除关联关系
        LambdaQueryWrapper<VendorUserRelationPO> relWrapper = Wrappers.lambdaQuery();
        relWrapper.eq(VendorUserRelationPO::getVendorId, vendorId);
        vendorUserRelationMapper.delete(relWrapper);

        // 删除厂商记录
        vendorMapper.deleteById(vendorId);

        return HttpResponseVO.<String>builder().code(HttpStatusConstants.SUCCESS).msg("草稿已删除").build();
    }

    /**
     * 获取当前厂商用户关联的厂商信息（通过上下文vendorId获取）
     */
    @Override
    public HttpResponseVO<VendorVO> getMyVendorInfo() {
        PcUserInfo userInfo = UserContext.get();
        if (userInfo.getRole() != PcUserIdentityEnum.VENDOR_USER) {
            return HttpResponseVO.<VendorVO>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("仅厂商用户可操作")
                    .build();
        }

        Integer vendorId = userInfo.getVendorId();
        if (vendorId == null) {
            return HttpResponseVO.<VendorVO>builder()
                    .code(HttpStatusConstants.SUCCESS)
                    .msg("暂未关联厂商")
                    .build();
        }

        VendorPO vendor = vendorMapper.selectById(vendorId);
        if (vendor == null) {
            return HttpResponseVO.<VendorVO>builder()
                    .code(HttpStatusConstants.SUCCESS)
                    .msg("暂未关联厂商")
                    .build();
        }

        return HttpResponseVO.<VendorVO>builder()
                .data(mapStructMapper.vendorPoToVo(vendor))
                .code(HttpStatusConstants.SUCCESS)
                .msg("获取厂商信息成功")
                .build();
    }

    /**
     * 根据审核记录ID获取审核流程进度
     */
    @Override
    public HttpResponseVO<AuditProgressVO> getAuditProgress(Integer auditRecordId) {
        VendorAuditRecordPO record = vendorAuditRecordMapper.selectById(auditRecordId);
        if (record == null) {
            return HttpResponseVO.<AuditProgressVO>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("审核记录不存在")
                    .build();
        }

        PcUserInfo userInfo = UserContext.get();
        if (userInfo.getRole() == PcUserIdentityEnum.VENDOR_USER
                && !isVendorUser(userInfo.getUserId(), record.getVendorId())) {
            return HttpResponseVO.<AuditProgressVO>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("无权查看该审核进度")
                    .build();
        }

        VendorPO vendor = vendorMapper.selectById(record.getVendorId());

        AuditProgressVO progressVO = new AuditProgressVO();
        progressVO.setVendorStatus(vendor != null ? vendor.getStatus().getValue() : null);
        progressVO.setCurrentRound(record.getRound());
        progressVO.setAuditRecord(mapStructMapper.auditRecordPoToVo(record));

        // 查询所有启用的审核节点，按顺序展示
        LambdaQueryWrapper<AuditNodePO> nodeWrapper = Wrappers.lambdaQuery();
        nodeWrapper.eq(AuditNodePO::getIsActive, true);
        nodeWrapper.orderByAsc(AuditNodePO::getOrder);
        List<AuditNodePO> allNodes = auditNodeMapper.selectList(nodeWrapper);

        // 查询该审核记录下已创建的任务
        LambdaQueryWrapper<AuditTaskPO> taskWrapper = Wrappers.lambdaQuery();
        taskWrapper.eq(AuditTaskPO::getAuditRecordId, auditRecordId);
        List<AuditTaskPO> tasks = auditTaskMapper.selectList(taskWrapper);
        // 按节点ID索引任务
        java.util.Map<Integer, AuditTaskPO> taskMap = tasks.stream()
                .collect(Collectors.toMap(AuditTaskPO::getAuditNodeId, t -> t));

        List<AuditInstanceVO> instanceVOs = new ArrayList<>();
        for (AuditNodePO node : allNodes) {
            AuditInstanceVO vo = new AuditInstanceVO();
            vo.setAuditNodeId(node.getAuditNodeId());
            vo.setNodeName(node.getName());
            vo.setNodeType(node.getType() != null ? node.getType().getValue() : null);

            AuditTaskPO task = taskMap.get(node.getAuditNodeId());
            if (task != null) {
                vo.setAuditTaskId(task.getAuditTaskId());
                vo.setVendorId(task.getVendorId());
                vo.setAuditRecordId(task.getAuditRecordId());
                vo.setCreatedTime(task.getCreatedTime());
                vo.setCompletedTime(task.getCompletedTime());
                vo.setPassed(task.getPassed());
                vo.setNotes(task.getNotes());
                if (task.getAdminId() != null) {
                    PlatformAdminPO admin = platformAdminMapper.selectById(task.getAdminId());
                    if (admin != null) {
                        vo.setAdminName(admin.getRealName() != null ? admin.getRealName() : admin.getUsername());
                    }
                }
            }
            instanceVOs.add(vo);
        }
        progressVO.setInstances(instanceVOs);

        return HttpResponseVO.<AuditProgressVO>builder()
                .data(progressVO)
                .code(HttpStatusConstants.SUCCESS)
                .msg("获取审核进度成功")
                .build();
    }

    /**
     * 获取当前厂商用户关联的所有厂商列表
     */
    @Override
    public HttpResponseVO<List<VendorVO>> getMyVendors() {
        PcUserInfo userInfo = UserContext.get();

        LambdaQueryWrapper<VendorUserRelationPO> relWrapper = Wrappers.lambdaQuery();
        relWrapper.eq(VendorUserRelationPO::getVendorUserId, userInfo.getUserId());
        List<VendorUserRelationPO> relations = vendorUserRelationMapper.selectList(relWrapper);

        if (relations == null || relations.isEmpty()) {
            return HttpResponseVO.<List<VendorVO>>builder()
                    .data(List.of())
                    .code(HttpStatusConstants.SUCCESS)
                    .msg("暂无关联厂商")
                    .build();
        }

        List<Integer> vendorIds = relations.stream()
                .map(VendorUserRelationPO::getVendorId)
                .collect(Collectors.toList());
        List<VendorPO> vendors = vendorMapper.selectBatchIds(vendorIds);

        List<VendorVO> voList = vendors.stream()
                .map(mapStructMapper::vendorPoToVo)
                .collect(Collectors.toList());

        return HttpResponseVO.<List<VendorVO>>builder()
                .data(voList)
                .code(HttpStatusConstants.SUCCESS)
                .msg("获取厂商列表成功")
                .build();
    }

    /**
     * 获取当前厂商用户关联厂商的审核记录列表
     * <p>从 vendor_audit_records 查询，公司信息从 data 快照解析
     */
    @Override
    public HttpResponseVO<Map<String, Object>> getMyAuditRecords(VendorProgressQueryDTO queryDTO) {
        PcUserInfo userInfo = UserContext.get();

        // 只查询当前用户发起的审核记录
        LambdaQueryWrapper<VendorAuditRecordPO> allWrapper = Wrappers.lambdaQuery();
        allWrapper.eq(VendorAuditRecordPO::getVendorUserId, userInfo.getUserId());
        List<VendorAuditRecordPO> allRecords = vendorAuditRecordMapper.selectList(allWrapper);

        if (allRecords == null || allRecords.isEmpty()) {
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("list", List.of());
            emptyResult.put("total", 0);
            emptyResult.put("pageNum", queryDTO.getPageNum());
            emptyResult.put("pageSize", queryDTO.getPageSize());
            return HttpResponseVO.<Map<String, Object>>builder()
                    .data(emptyResult)
                    .code(HttpStatusConstants.SUCCESS)
                    .msg("暂无审核记录")
                    .build();
        }

        // 每个厂商的最大轮次
        java.util.Map<Integer, Integer> maxRoundMap = allRecords.stream()
                .collect(Collectors.groupingBy(VendorAuditRecordPO::getVendorId,
                        Collectors.collectingAndThen(
                                Collectors.maxBy(java.util.Comparator.comparingInt(VendorAuditRecordPO::getRound)),
                                opt -> opt.map(VendorAuditRecordPO::getRound).orElse(0))));

        // 失败结果集合
        java.util.Set<AuditRecordResultEnum> failedResults = java.util.Set.of(
                AuditRecordResultEnum.QUALIFICATION_FAILED,
                AuditRecordResultEnum.FUNCTIONAL_TEST_FAILED,
                AuditRecordResultEnum.PERFORMANCE_TEST_FAILED,
                AuditRecordResultEnum.FINAL_APPROVAL_REJECTED);

        // 已结束的结果集合
        java.util.Set<AuditRecordResultEnum> finishedResults = new java.util.HashSet<>(failedResults);
        finishedResults.add(AuditRecordResultEnum.APPROVED);

        // 只返回已结束的记录
        List<VendorListVO> voList = allRecords.stream()
                .filter(r -> finishedResults.contains(r.getResult()))
                .sorted(java.util.Comparator.comparing(VendorAuditRecordPO::getCreatedTime).reversed())
                .map(record -> {
            VendorListVO vo = new VendorListVO();
            vo.setVendorId(record.getVendorId());
            vo.setAuditRecordId(record.getAuditRecordId());
            vo.setCurrentRound(record.getRound());
            vo.setResult(record.getResult());
            vo.setCreatedTime(record.getCreatedTime());
            vo.setCompletedTime(record.getCompletedTime());

            // 可重新申请：被驳回 且 是该厂商最高轮次
            vo.setCanResubmit(failedResults.contains(record.getResult())
                    && record.getRound().equals(maxRoundMap.get(record.getVendorId())));

            if (record.getData() instanceof java.util.Map<?, ?> dataMap) {
                vo.setCompanyName(getStr(dataMap, "companyName"));
                vo.setShortName(getStr(dataMap, "shortName"));
            }

            return vo;
        }).collect(Collectors.toList());

        // In-memory filtering
        List<VendorListVO> filteredList = voList.stream()
                .filter(vo -> {
                    if (StringUtils.hasText(queryDTO.getCompanyName())) {
                        if (vo.getCompanyName() == null
                                || !vo.getCompanyName().toLowerCase().contains(queryDTO.getCompanyName().toLowerCase())) {
                            return false;
                        }
                    }
                    if (queryDTO.getResult() != null) {
                        if (vo.getResult() != queryDTO.getResult()) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // In-memory pagination
        int total = filteredList.size();
        int pageNum = queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize();
        int fromIndex = Math.min((pageNum - 1) * pageSize, total);
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<VendorListVO> pagedList = filteredList.subList(fromIndex, toIndex);

        Map<String, Object> result = new HashMap<>();
        result.put("list", pagedList);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);

        return HttpResponseVO.<Map<String, Object>>builder()
                .data(result)
                .code(HttpStatusConstants.SUCCESS)
                .msg("获取审核记录列表成功")
                .build();
    }

    @Override
    public HttpResponseVO<String> refreshPlatformToken() {
        PcUserInfo userInfo = UserContext.get();
        Integer vendorId = userInfo.getVendorId();
        if (vendorId == null) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("当前用户未关联厂商")
                    .build();
        }

        VendorPO vendor = vendorMapper.selectById(vendorId);
        if (vendor == null) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("厂商不存在")
                    .build();
        }

        if (vendor.getStatus() != VendorStatusEnum.APPROVED) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("仅已审核通过的厂商可以刷新Token")
                    .build();
        }

        String newToken = "PAT-" + java.util.UUID.randomUUID().toString().replace("-", "");
        vendor.setPlatformAccessToken(newToken);
        vendorMapper.updateById(vendor);

        return HttpResponseVO.<String>builder()
                .data(newToken)
                .code(HttpStatusConstants.SUCCESS)
                .msg("平台访问Token已刷新")
                .build();
    }

    /**
     * 获取当前用户发起申请且已通过审核的厂商列表
     */
    @Override
    public HttpResponseVO<List<VendorVO>> getMyApprovedVendors() {
        PcUserInfo userInfo = UserContext.get();

        // 查询当前用户发起的、已通过审核的记录
        LambdaQueryWrapper<VendorAuditRecordPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(VendorAuditRecordPO::getVendorUserId, userInfo.getUserId());
        wrapper.eq(VendorAuditRecordPO::getResult, AuditRecordResultEnum.APPROVED);
        List<VendorAuditRecordPO> approvedRecords = vendorAuditRecordMapper.selectList(wrapper);

        if (approvedRecords == null || approvedRecords.isEmpty()) {
            return HttpResponseVO.<List<VendorVO>>builder()
                    .data(List.of())
                    .code(HttpStatusConstants.SUCCESS)
                    .msg("暂无已通过的资质")
                    .build();
        }

        // 取去重的vendorId
        List<Integer> vendorIds = approvedRecords.stream()
                .map(VendorAuditRecordPO::getVendorId)
                .distinct()
                .collect(Collectors.toList());
        List<VendorPO> vendors = vendorMapper.selectBatchIds(vendorIds);

        List<VendorVO> voList = vendors.stream()
                .map(mapStructMapper::vendorPoToVo)
                .collect(Collectors.toList());

        return HttpResponseVO.<List<VendorVO>>builder()
                .data(voList)
                .code(HttpStatusConstants.SUCCESS)
                .msg("获取已通过资质列表成功")
                .build();
    }

    private String getStr(java.util.Map<?, ?> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }

    // ========== 私有方法 ==========

    private VendorPO getVendorByUserId(Integer userId) {
        LambdaQueryWrapper<VendorUserRelationPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(VendorUserRelationPO::getVendorUserId, userId);
        wrapper.eq(VendorUserRelationPO::getIsMain, true);
        wrapper.last("LIMIT 1");
        VendorUserRelationPO relation = vendorUserRelationMapper.selectOne(wrapper);
        if (relation == null) {
            return null;
        }
        return vendorMapper.selectById(relation.getVendorId());
    }

    private boolean isVendorUser(Integer userId, Integer vendorId) {
        LambdaQueryWrapper<VendorUserRelationPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(VendorUserRelationPO::getVendorUserId, userId);
        wrapper.eq(VendorUserRelationPO::getVendorId, vendorId);
        return vendorUserRelationMapper.selectCount(wrapper) > 0;
    }

    private void copyDtoToVendor(SubmitVendorDTO dto, VendorPO vendor) {
        vendor.setCompanyName(dto.getCompanyName());
        vendor.setShortName(dto.getShortName());
        vendor.setLicenseNo(dto.getLicenseNo());
        vendor.setLicenseImage(dto.getLicenseImage());
        vendor.setLegalPerson(dto.getLegalPerson());
        vendor.setLegalPersonId(dto.getLegalPersonId());
        vendor.setContactPerson(dto.getContactPerson());
        vendor.setContactPhone(dto.getContactPhone());
        vendor.setContactEmail(dto.getContactEmail());
        vendor.setCompanyAddress(dto.getCompanyAddress());
        vendor.setWebsite(dto.getWebsite());
        vendor.setIntroduction(dto.getIntroduction());
        vendor.setBusinessScope(dto.getBusinessScope());
        vendor.setApiEndpoint(dto.getApiEndpoint());
        vendor.setVendorAccessToken(dto.getVendorAccessToken());
    }
}

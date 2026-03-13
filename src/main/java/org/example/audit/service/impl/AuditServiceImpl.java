package org.example.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.audit.dto.AuditRecordQueryDTO;
import org.example.audit.dto.FinalApprovalDTO;
import org.example.audit.dto.PerformanceTestDTO;
import org.example.audit.dto.QualificationAuditDTO;
import org.example.audit.dto.FunctionalTestDTO;
import org.example.audit.dto.VendorAuditQueryDTO;
import org.example.audit.enums.AuditTaskStatusEnum;
import org.example.audit.mapper.*;
import org.example.audit.po.*;
import org.example.audit.service.AuditService;
import org.example.audit.statemachine.AuditEvent;
import org.example.audit.statemachine.AuditStateMachine;
import org.example.audit.vo.AuditTaskVO;
import org.example.audit.vo.VendorAuditRecordVO;
import org.example.audit.vo.VendorListVO;
import org.example.audit.vo.VendorVO;
import org.example.auth.common.PcUserInfo;
import org.example.auth.common.UserContext;
import org.example.auth.constants.HttpStatusConstants;
import org.example.auth.mapper.PlatformAdminMapper;
import org.example.auth.mapper.VendorUserMapper;
import org.example.auth.po.PlatformAdminPO;
import org.example.auth.po.VendorUserPO;
import org.example.auth.vo.HttpResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 审核服务实现（平台管理员端）
 * <p>审核流程相关操作统一委托给 {@link AuditStateMachine} 处理
 *
 * @author fasonghao
 */
@Service
public class AuditServiceImpl implements AuditService {

    @Autowired
    private VendorMapper vendorMapper;

    @Autowired
    private VendorAuditRecordMapper vendorAuditRecordMapper;

    @Autowired
    private AuditMapStructMapper mapStructMapper;

    @Autowired
    private AuditStateMachine stateMachine;

    @Autowired
    private AuditTaskMapper auditTaskMapper;

    @Autowired
    private AuditNodeMapper auditNodeMapper;

    @Autowired
    private PlatformAdminMapper platformAdminMapper;

    @Autowired
    private VendorUserMapper vendorUserMapper;

    /**
     * 获取厂商审核列表（支持条件查询+分页）
     * <p>查询 vendor_audit_records 中 admin_id 为当前管理员的记录
     * <p>公司信息从 data 快照中解析，不受后续修改影响
     */
    @Override
    public HttpResponseVO<Map<String, Object>> getVendorList(VendorAuditQueryDTO queryDTO) {
        PcUserInfo userInfo = UserContext.get();

        // 查询当前管理员负责的审核记录
        LambdaQueryWrapper<VendorAuditRecordPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(VendorAuditRecordPO::getAdminId, userInfo.getUserId());
        wrapper.orderByDesc(VendorAuditRecordPO::getCreatedTime);
        List<VendorAuditRecordPO> records = vendorAuditRecordMapper.selectList(wrapper);

        List<VendorListVO> voList = records.stream().map(record -> {
            VendorListVO vo = new VendorListVO();
            vo.setVendorId(record.getVendorId());
            vo.setAuditRecordId(record.getAuditRecordId());
            vo.setCurrentRound(record.getRound());
            vo.setResult(record.getResult());
            vo.setCreatedTime(record.getCreatedTime());
            vo.setCompletedTime(record.getCompletedTime());

            // 从 data 快照中解析公司信息
            if (record.getData() instanceof java.util.Map<?, ?> dataMap) {
                vo.setCompanyName(getStr(dataMap, "companyName"));
                vo.setShortName(getStr(dataMap, "shortName"));
                vo.setContactPerson(getStr(dataMap, "contactPerson"));
                vo.setContactPhone(getStr(dataMap, "contactPhone"));
            }

            // 填充申请用户名称
            if (record.getVendorUserId() != null) {
                VendorUserPO vendorUser = vendorUserMapper.selectById(record.getVendorUserId());
                if (vendorUser != null) {
                    vo.setVendorUserName(vendorUser.getRealName() != null ? vendorUser.getRealName() : vendorUser.getUsername());
                }
            }

            // 从厂商表获取当前状态和提交时间
            VendorPO vendor = vendorMapper.selectById(record.getVendorId());
            if (vendor != null) {
                vo.setStatus(vendor.getStatus());
                vo.setSubmittedTime(vendor.getSubmittedTime());
            }

            return vo;
        }).collect(Collectors.toList());

        // 条件过滤
        List<VendorListVO> filteredList = voList.stream()
                .filter(vo -> {
                    if (queryDTO.getCompanyName() != null && !queryDTO.getCompanyName().isEmpty()) {
                        if (vo.getCompanyName() == null ||
                                !vo.getCompanyName().toLowerCase().contains(queryDTO.getCompanyName().toLowerCase())) {
                            return false;
                        }
                    }
                    if (queryDTO.getResult() != null) {
                        if (vo.getResult() != queryDTO.getResult()) {
                            return false;
                        }
                    }
                    if (queryDTO.getRound() != null) {
                        if (!queryDTO.getRound().equals(vo.getCurrentRound())) {
                            return false;
                        }
                    }
                    if (queryDTO.getSubmitTimeStart() != null) {
                        if (vo.getSubmittedTime() == null || vo.getSubmittedTime().isBefore(queryDTO.getSubmitTimeStart())) {
                            return false;
                        }
                    }
                    if (queryDTO.getSubmitTimeEnd() != null) {
                        if (vo.getSubmittedTime() == null || vo.getSubmittedTime().isAfter(queryDTO.getSubmitTimeEnd())) {
                            return false;
                        }
                    }
                    if (queryDTO.getCompletedTimeStart() != null) {
                        if (vo.getCompletedTime() == null || vo.getCompletedTime().isBefore(queryDTO.getCompletedTimeStart())) {
                            return false;
                        }
                    }
                    if (queryDTO.getCompletedTimeEnd() != null) {
                        if (vo.getCompletedTime() == null || vo.getCompletedTime().isAfter(queryDTO.getCompletedTimeEnd())) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // 内存分页
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
                .msg("获取厂商列表成功")
                .build();
    }

    private String getStr(java.util.Map<?, ?> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }

    /**
     * 获取厂商详情
     * <p>传 recordId 时从审核记录的 data 快照中读取厂商信息，不传则从厂商表读取
     */
    @Override
    public HttpResponseVO<VendorVO> getVendorDetail(Integer vendorId, Integer recordId) {
        if (recordId != null) {
            // 从审核记录快照读取
            VendorAuditRecordPO record = vendorAuditRecordMapper.selectById(recordId);
            if (record == null || !record.getVendorId().equals(vendorId)) {
                return HttpResponseVO.<VendorVO>builder()
                        .code(HttpStatusConstants.ERROR)
                        .msg("审核记录不存在")
                        .build();
            }
            VendorVO vo = parseSnapshotToVendorVO(record);
            return HttpResponseVO.<VendorVO>builder()
                    .data(vo)
                    .code(HttpStatusConstants.SUCCESS)
                    .msg("获取厂商详情成功")
                    .build();
        }

        VendorPO vendor = vendorMapper.selectById(vendorId);
        if (vendor == null) {
            return HttpResponseVO.<VendorVO>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("厂商不存在")
                    .build();
        }
        return HttpResponseVO.<VendorVO>builder()
                .data(mapStructMapper.vendorPoToVo(vendor))
                .code(HttpStatusConstants.SUCCESS)
                .msg("获取厂商详情成功")
                .build();
    }

    /**
     * 从审核记录的 data 快照解析厂商信息
     */
    private VendorVO parseSnapshotToVendorVO(VendorAuditRecordPO record) {
        VendorVO vo = new VendorVO();
        vo.setVendorId(record.getVendorId());
        if (record.getData() instanceof java.util.Map<?, ?> dataMap) {
            vo.setCompanyName(getStr(dataMap, "companyName"));
            vo.setShortName(getStr(dataMap, "shortName"));
            vo.setLicenseNo(getStr(dataMap, "licenseNo"));
            vo.setLicenseImage(getStr(dataMap, "licenseImage"));
            vo.setLegalPerson(getStr(dataMap, "legalPerson"));
            vo.setLegalPersonId(getStr(dataMap, "legalPersonId"));
            vo.setContactPerson(getStr(dataMap, "contactPerson"));
            vo.setContactPhone(getStr(dataMap, "contactPhone"));
            vo.setContactEmail(getStr(dataMap, "contactEmail"));
            vo.setCompanyAddress(getStr(dataMap, "companyAddress"));
            vo.setWebsite(getStr(dataMap, "website"));
            vo.setIntroduction(getStr(dataMap, "introduction"));
            vo.setBusinessScope(getStr(dataMap, "businessScope"));
            vo.setApiEndpoint(getStr(dataMap, "apiEndpoint"));
            vo.setVendorAccessToken(getStr(dataMap, "vendorAccessToken"));
        }
        // 从厂商表补充状态信息
        VendorPO vendor = vendorMapper.selectById(record.getVendorId());
        if (vendor != null) {
            vo.setStatus(vendor.getStatus());
        }
        return vo;
    }

    /**
     * 获取厂商的审核记录列表
     */
    @Override
    public HttpResponseVO<List<VendorAuditRecordVO>> getAuditRecords(Integer vendorId) {
        LambdaQueryWrapper<VendorAuditRecordPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(VendorAuditRecordPO::getVendorId, vendorId);
        wrapper.orderByDesc(VendorAuditRecordPO::getRound);
        List<VendorAuditRecordPO> records = vendorAuditRecordMapper.selectList(wrapper);

        List<VendorAuditRecordVO> voList = records.stream()
                .map(record -> {
                    VendorAuditRecordVO vo = mapStructMapper.auditRecordPoToVo(record);
                    if (record.getAdminId() != null) {
                        PlatformAdminPO admin = platformAdminMapper.selectById(record.getAdminId());
                        if (admin != null) {
                            vo.setAdminName(admin.getRealName() != null ? admin.getRealName() : admin.getUsername());
                        }
                    }
                    return vo;
                })
                .collect(Collectors.toList());

        return HttpResponseVO.<List<VendorAuditRecordVO>>builder()
                .data(voList)
                .code(HttpStatusConstants.SUCCESS)
                .msg("获取审核记录成功")
                .build();
    }

    /**
     * 获取当前管理员的审核完成记录（支持条件查询+分页）
     */
    @Override
    public HttpResponseVO<Map<String, Object>> getMyAuditRecords(AuditRecordQueryDTO queryDTO) {
        PcUserInfo userInfo = UserContext.get();

        LambdaQueryWrapper<AuditTaskPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AuditTaskPO::getAdminId, userInfo.getUserId());
        wrapper.eq(AuditTaskPO::getStatus, AuditTaskStatusEnum.COMPLETED);
        wrapper.orderByDesc(AuditTaskPO::getCompletedTime);
        List<AuditTaskPO> tasks = auditTaskMapper.selectList(wrapper);

        List<AuditTaskVO> voList = tasks.stream()
                .map(this::convertTaskToVO)
                .collect(Collectors.toList());

        // 条件过滤
        List<AuditTaskVO> filteredList = voList.stream()
                .filter(vo -> {
                    if (queryDTO.getCompanyName() != null && !queryDTO.getCompanyName().isEmpty()) {
                        if (vo.getCompanyName() == null ||
                                !vo.getCompanyName().toLowerCase().contains(queryDTO.getCompanyName().toLowerCase())) {
                            return false;
                        }
                    }
                    if (queryDTO.getRound() != null) {
                        if (!queryDTO.getRound().equals(vo.getRound())) {
                            return false;
                        }
                    }
                    if (queryDTO.getAuditNodeId() != null) {
                        if (!queryDTO.getAuditNodeId().equals(vo.getAuditNodeId())) {
                            return false;
                        }
                    }
                    if (queryDTO.getPassed() != null) {
                        if (vo.getPassed() == null || !vo.getPassed().equals(queryDTO.getPassed())) {
                            return false;
                        }
                    }
                    if (queryDTO.getCompletedTimeStart() != null) {
                        if (vo.getCompletedTime() == null || vo.getCompletedTime().isBefore(queryDTO.getCompletedTimeStart())) {
                            return false;
                        }
                    }
                    if (queryDTO.getCompletedTimeEnd() != null) {
                        if (vo.getCompletedTime() == null || vo.getCompletedTime().isAfter(queryDTO.getCompletedTimeEnd())) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // 内存分页
        int total = filteredList.size();
        int pageNum = queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize();
        int fromIndex = Math.min((pageNum - 1) * pageSize, total);
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<AuditTaskVO> pagedList = filteredList.subList(fromIndex, toIndex);

        Map<String, Object> result = new HashMap<>();
        result.put("list", pagedList);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);

        return HttpResponseVO.<Map<String, Object>>builder()
                .data(result)
                .code(HttpStatusConstants.SUCCESS)
                .msg("获取审核记录成功")
                .build();
    }

    /**
     * 任务PO转VO（审核记录页使用）
     */
    private AuditTaskVO convertTaskToVO(AuditTaskPO po) {
        AuditTaskVO vo = mapStructMapper.auditTaskPoToVo(po);

        AuditNodePO node = auditNodeMapper.selectById(po.getAuditNodeId());
        if (node != null) {
            vo.setNodeName(node.getName());
            vo.setNodeType(node.getType());
        }

        vo.setVendorId(po.getVendorId());
        VendorPO vendor = vendorMapper.selectById(po.getVendorId());
        if (vendor != null) {
            vo.setCompanyName(vendor.getCompanyName());
        }
        VendorAuditRecordPO record = vendorAuditRecordMapper.selectById(po.getAuditRecordId());
        if (record != null) {
            vo.setRound(record.getRound());
        }

        return vo;
    }

    /**
     * 资质审核（根据审核结果自动决定事件）
     */
    @Override
    public HttpResponseVO<String> qualificationAudit(Integer vendorId, QualificationAuditDTO dto) {
        AuditEvent event = dto.getPassed()
                ? AuditEvent.QUALIFICATION_PASS
                : AuditEvent.QUALIFICATION_FAIL;
        return stateMachine.fire(vendorId, event, dto);
    }

    /**
     * 功能测试审核（根据是否通过自动决定事件）
     */
    @Override
    public HttpResponseVO<String> functionalTestAudit(Integer vendorId, FunctionalTestDTO dto) {
        AuditEvent event = dto.getPassed()
                ? AuditEvent.FUNCTIONAL_TEST_PASS
                : AuditEvent.FUNCTIONAL_TEST_FAIL;
        return stateMachine.fire(vendorId, event, dto);
    }

    /**
     * 性能测试审核（根据是否通过自动决定事件）
     */
    @Override
    public HttpResponseVO<String> performanceTest(Integer vendorId, PerformanceTestDTO dto) {
        AuditEvent event = dto.getPassed()
                ? AuditEvent.PERFORMANCE_PASS
                : AuditEvent.PERFORMANCE_FAIL;
        return stateMachine.fire(vendorId, event, dto);
    }

    /**
     * 最终审批（根据是否批准自动决定事件）
     */
    @Override
    public HttpResponseVO<String> finalApproval(Integer vendorId, FinalApprovalDTO dto) {
        AuditEvent event = dto.getApproved()
                ? AuditEvent.FINAL_APPROVE
                : AuditEvent.FINAL_REJECT;
        return stateMachine.fire(vendorId, event, dto);
    }

}

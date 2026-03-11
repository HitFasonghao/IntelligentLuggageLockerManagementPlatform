package org.example.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.audit.dto.VendorOperationDTO;
import org.example.audit.enums.VendorStatusEnum;
import org.example.audit.mapper.AuditMapStructMapper;
import org.example.audit.mapper.VendorMapper;
import org.example.audit.po.VendorPO;
import org.example.audit.service.VendorManagementService;
import org.example.audit.statemachine.AuditEvent;
import org.example.audit.statemachine.AuditStateMachine;
import org.example.audit.vo.VendorListVO;
import org.example.audit.vo.VendorVO;
import org.example.auth.constants.HttpStatusConstants;
import org.example.auth.vo.HttpResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 厂商管理服务实现（已入驻厂商的日常管理）
 * @author fasonghao
 */
@Service
public class VendorManagementServiceImpl implements VendorManagementService {

    @Autowired
    private VendorMapper vendorMapper;

    @Autowired
    private AuditMapStructMapper mapStructMapper;

    @Autowired
    private AuditStateMachine stateMachine;

    @Override
    public HttpResponseVO<List<VendorListVO>> getApprovedVendors() {
        LambdaQueryWrapper<VendorPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(VendorPO::getStatus, VendorStatusEnum.APPROVED);
        wrapper.orderByDesc(VendorPO::getApprovedTime);
        return buildVendorListResponse(wrapper);
    }

    @Override
    public HttpResponseVO<List<VendorListVO>> getAbnormalVendors() {
        LambdaQueryWrapper<VendorPO> wrapper = Wrappers.lambdaQuery();
        wrapper.in(VendorPO::getStatus, VendorStatusEnum.SUSPENDED, VendorStatusEnum.BANNED);
        wrapper.orderByDesc(VendorPO::getUpdatedTime);
        return buildVendorListResponse(wrapper);
    }

    @Override
    public HttpResponseVO<VendorVO> getVendorDetail(Integer vendorId) {
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

    @Override
    public HttpResponseVO<String> suspendVendor(Integer vendorId, VendorOperationDTO dto) {
        return stateMachine.fire(vendorId, AuditEvent.SUSPEND, dto);
    }

    @Override
    public HttpResponseVO<String> restoreVendor(Integer vendorId, VendorOperationDTO dto) {
        return stateMachine.fire(vendorId, AuditEvent.RESTORE, dto);
    }

    @Override
    public HttpResponseVO<String> banVendor(Integer vendorId, VendorOperationDTO dto) {
        return stateMachine.fire(vendorId, AuditEvent.BAN, dto);
    }

    private HttpResponseVO<List<VendorListVO>> buildVendorListResponse(LambdaQueryWrapper<VendorPO> wrapper) {
        List<VendorPO> vendors = vendorMapper.selectList(wrapper);
        List<VendorListVO> voList = vendors.stream().map(v -> {
            VendorListVO vo = new VendorListVO();
            vo.setVendorId(v.getVendorId());
            vo.setCompanyName(v.getCompanyName());
            vo.setShortName(v.getShortName());
            vo.setContactPerson(v.getContactPerson());
            vo.setContactPhone(v.getContactPhone());
            vo.setStatus(v.getStatus());
            vo.setSubmittedTime(v.getSubmittedTime());
            vo.setCreatedTime(v.getCreatedTime());
            return vo;
        }).collect(Collectors.toList());

        return HttpResponseVO.<List<VendorListVO>>builder()
                .data(voList)
                .code(HttpStatusConstants.SUCCESS)
                .msg("获取厂商列表成功")
                .build();
    }
}

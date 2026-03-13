package org.example.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.audit.mapper.VendorUserRelationMapper;
import org.example.audit.po.VendorUserRelationPO;
import org.example.audit.service.VendorUserRelationService;
import org.example.audit.vo.VendorUserRelationVO;
import org.example.auth.common.PcUserInfo;
import org.example.auth.common.UserContext;
import org.example.auth.constants.HttpStatusConstants;
import org.example.auth.enums.PcUserIdentityEnum;
import org.example.auth.mapper.VendorUserMapper;
import org.example.auth.po.VendorUserPO;
import org.example.auth.vo.HttpResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 厂商用户关联服务实现
 * @author fasonghao
 */
@Service
public class VendorUserRelationServiceImpl implements VendorUserRelationService {

    @Autowired
    private VendorUserRelationMapper vendorUserRelationMapper;

    @Autowired
    private VendorUserMapper vendorUserMapper;

    /**
     * 获取厂商下的用户列表
     */
    @Override
    public HttpResponseVO<List<VendorUserRelationVO>> getVendorUsers() {
        PcUserInfo userInfo = UserContext.get();
        Integer vendorId = userInfo.getVendorId();

        // 校验是否为该厂商的主管理员
        if (!isMainAdmin(userInfo.getUserId(), vendorId)) {
            return HttpResponseVO.<List<VendorUserRelationVO>>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("仅主管理员可查看用户列表")
                    .build();
        }

        LambdaQueryWrapper<VendorUserRelationPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(VendorUserRelationPO::getVendorId, vendorId);
        List<VendorUserRelationPO> relations = vendorUserRelationMapper.selectList(wrapper);

        List<VendorUserRelationVO> voList = relations.stream().map(r -> {
            VendorUserRelationVO vo = new VendorUserRelationVO();
            vo.setVendorUserRelationId(r.getVendorUserRelationId());
            vo.setVendorUserId(r.getVendorUserId());
            vo.setVendorId(r.getVendorId());
            vo.setIsMain(r.getIsMain());
            // 查询用户信息
            VendorUserPO user = vendorUserMapper.selectById(r.getVendorUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setRealName(user.getRealName());
                vo.setPhone(user.getPhone());
            }
            return vo;
        }).collect(Collectors.toList());

        return HttpResponseVO.<List<VendorUserRelationVO>>builder()
                .data(voList)
                .code(HttpStatusConstants.SUCCESS)
                .msg("获取厂商用户列表成功")
                .build();
    }

    /**
     * 添加厂商用户（通过用户名）
     */
    @Override
    public HttpResponseVO<String> addVendorUser(String username) {
        PcUserInfo userInfo = UserContext.get();
        Integer vendorId = userInfo.getVendorId();

        // 校验是否为该厂商的主管理员
        if (!isMainAdmin(userInfo.getUserId(), vendorId)) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("仅主管理员可添加用户")
                    .build();
        }

        // 通过用户名查找用户
        LambdaQueryWrapper<VendorUserPO> userWrapper = Wrappers.lambdaQuery();
        userWrapper.eq(VendorUserPO::getUsername, username);
        VendorUserPO user = vendorUserMapper.selectOne(userWrapper);
        if (user == null) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("用户不存在")
                    .build();
        }

        // 校验是否已关联
        LambdaQueryWrapper<VendorUserRelationPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(VendorUserRelationPO::getVendorId, vendorId);
        wrapper.eq(VendorUserRelationPO::getVendorUserId, user.getVendorUserId());
        if (vendorUserRelationMapper.selectCount(wrapper) > 0) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("该用户已关联该厂商")
                    .build();
        }

        VendorUserRelationPO relation = new VendorUserRelationPO();
        relation.setVendorId(vendorId);
        relation.setVendorUserId(user.getVendorUserId());
        relation.setIsMain(false);
        vendorUserRelationMapper.insert(relation);

        return HttpResponseVO.<String>builder()
                .code(HttpStatusConstants.SUCCESS)
                .msg("添加用户成功")
                .build();
    }

    /**
     * 移除厂商用户
     */
    @Override
    public HttpResponseVO<String> removeVendorUser(Integer vendorUserRelationId) {
        PcUserInfo userInfo = UserContext.get();
        Integer vendorId = userInfo.getVendorId();

        // 校验是否为该厂商的主管理员
        if (!isMainAdmin(userInfo.getUserId(), vendorId)) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("仅主管理员可移除用户")
                    .build();
        }

        // 查询关联关系
        VendorUserRelationPO relation = vendorUserRelationMapper.selectById(vendorUserRelationId);
        if (relation == null || !relation.getVendorId().equals(vendorId)) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("关联关系不存在")
                    .build();
        }

        // 不允许移除自己（主管理员）
        if (relation.getVendorUserId().equals(userInfo.getUserId())) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("不能移除自己")
                    .build();
        }

        int result = vendorUserRelationMapper.deleteById(vendorUserRelationId);

        if (result > 0) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.SUCCESS)
                    .msg("移除用户成功")
                    .build();
        }
        return HttpResponseVO.<String>builder()
                .code(HttpStatusConstants.ERROR)
                .msg("移除用户失败")
                .build();
    }

    /**
     * 判断是否为指定厂商的主管理员
     */
    private boolean isMainAdmin(Integer userId, Integer vendorId) {
        LambdaQueryWrapper<VendorUserRelationPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(VendorUserRelationPO::getVendorUserId, userId);
        wrapper.eq(VendorUserRelationPO::getVendorId, vendorId);
        wrapper.eq(VendorUserRelationPO::getIsMain, true);
        return vendorUserRelationMapper.selectCount(wrapper) > 0;
    }
}

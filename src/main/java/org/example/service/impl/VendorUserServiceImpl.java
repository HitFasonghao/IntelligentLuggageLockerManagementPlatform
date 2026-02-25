package org.example.service.impl;

import org.example.common.PcUserInfo;
import org.example.common.UserContext;
import org.example.mapper.VendorUserMapper;
import org.example.po.VendorUserPO;
import org.example.service.VendorUserService;
import org.example.vo.HttpResponseVO;
import org.example.vo.VendorUserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author fasonghao
 */
@Service
public class VendorUserServiceImpl implements VendorUserService {

    @Autowired
    private VendorUserMapper vendorUserMapper;

    @Override
    public HttpResponseVO<VendorUserInfoVO> queryVendorUserInfo() {
        //从线程中获取用户上下文信息
        PcUserInfo userInfo = UserContext.get();
        VendorUserPO vendorUser=vendorUserMapper.selectById(userInfo.getUserId());

        return null;
    }
}

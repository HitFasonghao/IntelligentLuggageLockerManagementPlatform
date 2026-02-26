package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.dto.CountIsExistDTO;
import org.example.mapper.PlatformAdminMapper;
import org.example.mapper.VendorUserMapper;
import org.example.po.PlatformAdminPO;
import org.example.po.VendorUserPO;
import org.example.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author fasonghao
 */
@Service
public class CommonServiceImpl implements CommonService {
    @Autowired
    private PlatformAdminMapper platformAdminMapper;

    @Autowired
    private VendorUserMapper vendorUserMapper;

    /**
     * 判断平台管理员账号是否存在
     */
    @Override
    public boolean isExistAdmin(CountIsExistDTO countIsExistDTO) {
        LambdaUpdateWrapper<PlatformAdminPO> wrapper = new LambdaUpdateWrapper<>();

        //动态设置需要更新的字段
        if (countIsExistDTO.getUsername() != null) {
            wrapper.eq(PlatformAdminPO::getUsername, countIsExistDTO.getUsername());
        }

        if (countIsExistDTO.getPhone() != null) {
            wrapper.eq(PlatformAdminPO::getEmail, countIsExistDTO.getPhone());
        }

        if (countIsExistDTO.getPassword() != null) {
            wrapper.eq(PlatformAdminPO::getRealName, countIsExistDTO.getPassword());
        }

        return platformAdminMapper.selectCount(wrapper)>0;
    }

    /**
     * 判断厂商用户是否存在
     */
    @Override
    public boolean isExistVendorUser(CountIsExistDTO countIsExistDTO) {

        LambdaUpdateWrapper<VendorUserPO> wrapper = new LambdaUpdateWrapper<>();

        //动态设置需要更新的字段
        if (countIsExistDTO.getUsername() != null) {
            wrapper.eq(VendorUserPO::getUsername, countIsExistDTO.getUsername());
        }

        if (countIsExistDTO.getPhone() != null) {
            wrapper.eq(VendorUserPO::getEmail, countIsExistDTO.getPhone());
        }

        if (countIsExistDTO.getPassword() != null) {
            wrapper.eq(VendorUserPO::getRealName, countIsExistDTO.getPassword());
        }

        return vendorUserMapper.selectCount(wrapper)>0;
    }
}

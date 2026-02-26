package org.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.auth.common.PcUserInfo;
import org.example.auth.common.UserContext;
import org.example.auth.dto.CountIsExistDTO;
import org.example.auth.dto.UpdateAdminDTO;
import org.example.auth.mapper.MapStructMapper;
import org.example.auth.mapper.PlatformAdminMapper;
import org.example.auth.po.PlatformAdminPO;
import org.example.auth.service.AdminInfoService;
import org.example.auth.service.CommonService;
import org.example.auth.vo.AdminInfoVO;
import org.example.auth.vo.HttpResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * @author fasonghao
 */
@Service
public class AdminInfoServiceImpl implements AdminInfoService {

    @Autowired
    private PlatformAdminMapper platformAdminMapper;

    @Autowired
    private MapStructMapper mapStructMapper;

    @Autowired
    private CommonService commonService;

    /**
     * 查询账号装态
     */
    @Override
    public HttpResponseVO<AdminInfoVO> queryAdminInfo() {
        //从线程中获取用户上下文信息
        PcUserInfo userInfo = UserContext.get();
        //查询厂商用户信息
        PlatformAdminPO platformAdmin=platformAdminMapper.selectById(userInfo.getUserId());
        //对象映射
        AdminInfoVO adminInfoVO=mapStructMapper.adminToInfoVO(platformAdmin);
        return HttpResponseVO.<AdminInfoVO>builder()
                .data(adminInfoVO)
                .code(HttpStatus.OK.value())
                .msg("账号信息查询成功")
                .build();
    }

    /**
     * 修改账号状态
     */
    @Override
    public HttpResponseVO<String> updateAdminInfo(UpdateAdminDTO updateVendorUserDTO) {
        //判断用户名是否存在
        CountIsExistDTO countIsExistDTO=new CountIsExistDTO();
        countIsExistDTO.setUsername(updateVendorUserDTO.getUsername());
        if(commonService.isExistAdmin(countIsExistDTO)||commonService.isExistVendorUser(countIsExistDTO)){
            return HttpResponseVO.<String>builder()
                    .code(HttpStatus.CONFLICT.value())
                    .msg("用户名重复")
                    .build();
        }

        PcUserInfo userInfo = UserContext.get();

        LambdaUpdateWrapper<PlatformAdminPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PlatformAdminPO::getAdminId, userInfo.getUserId());

        //动态设置需要更新的字段
        if (updateVendorUserDTO.getUsername() != null) {
            wrapper.set(PlatformAdminPO::getUsername, updateVendorUserDTO.getUsername());
        }

        if (updateVendorUserDTO.getEmail() != null) {
            wrapper.set(PlatformAdminPO::getEmail, updateVendorUserDTO.getEmail());
        }

        if (updateVendorUserDTO.getRealName() != null) {
            wrapper.set(PlatformAdminPO::getRealName, updateVendorUserDTO.getRealName());
        }

        int code = platformAdminMapper.update(null, wrapper);
        if (code>0) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatus.OK.value())
                    .msg("账号信息修改成功")
                    .build();
        }else{
            return HttpResponseVO.<String>builder()
                    .code(HttpStatus.NOT_FOUND.value())
                    .msg("更新失败，用户不存在或数据库发生变化")
                    .build();
        }
    }

}

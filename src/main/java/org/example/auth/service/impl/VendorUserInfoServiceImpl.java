package org.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.auth.common.PcUserInfo;
import org.example.auth.common.UserContext;
import org.example.auth.dto.CountIsExistDTO;
import org.example.auth.dto.UpdateVendorUserDTO;
import org.example.auth.mapper.MapStructMapper;
import org.example.auth.mapper.VendorUserMapper;
import org.example.auth.po.VendorUserPO;
import org.example.auth.service.CommonService;
import org.example.auth.service.VendorUserInfoService;
import org.example.auth.vo.HttpResponseVO;
import org.example.auth.vo.VendorUserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * @author fasonghao
 */
@Service
public class VendorUserInfoServiceImpl implements VendorUserInfoService {

    @Autowired
    private VendorUserMapper vendorUserMapper;

    @Autowired
    private MapStructMapper mapStructMapper;

    @Autowired
    private CommonService commonService;

    /**
     * 查询账号信息
     */
    @Override
    public HttpResponseVO<VendorUserInfoVO> queryVendorUserInfo() {
        //从线程中获取用户上下文信息
        PcUserInfo userInfo = UserContext.get();
        //查询厂商用户信息
        VendorUserPO vendorUser=vendorUserMapper.selectById(userInfo.getUserId());
        //对象映射
        VendorUserInfoVO vendorUserInfoVO=mapStructMapper.vendorUserToInfoVO(vendorUser);
        return HttpResponseVO.<VendorUserInfoVO>builder()
                .data(vendorUserInfoVO)
                .code(HttpStatus.OK.value())
                .msg("账号信息查询成功")
                .build();
    }

    /**
     * 修改账号信息
     */
    @Override
    public HttpResponseVO<String> updateVendorUserInfo(UpdateVendorUserDTO updateVendorUserDTO) {
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

        LambdaUpdateWrapper<VendorUserPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(VendorUserPO::getVendorUserId, userInfo.getUserId());

        //动态设置需要更新的字段
        if (updateVendorUserDTO.getUsername() != null) {
            wrapper.set(VendorUserPO::getUsername, updateVendorUserDTO.getUsername());
        }

        if (updateVendorUserDTO.getEmail() != null) {
            wrapper.set(VendorUserPO::getEmail, updateVendorUserDTO.getEmail());
        }

        if (updateVendorUserDTO.getRealName() != null) {
            wrapper.set(VendorUserPO::getRealName, updateVendorUserDTO.getRealName());
        }

        //第一个参数传null，表示不通过实体类设置值，完全依赖wrapper.set()
        int code = vendorUserMapper.update(null, wrapper);

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

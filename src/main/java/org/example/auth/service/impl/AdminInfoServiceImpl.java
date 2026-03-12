package org.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.audit.service.FileStorageService;
import org.example.auth.common.PcUserInfo;
import org.example.auth.common.UserContext;
import org.example.auth.constants.HttpStatusConstants;
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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author fasonghao
 */
@Slf4j
@Service
public class AdminInfoServiceImpl implements AdminInfoService {

    @Autowired
    private PlatformAdminMapper platformAdminMapper;

    @Autowired
    private MapStructMapper mapStructMapper;

    @Autowired
    private CommonService commonService;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 查询账号装态
     */
    @Override
    public HttpResponseVO<AdminInfoVO> queryAdminInfo() {
        //从线程中获取用户上下文信息
        PcUserInfo userInfo = UserContext.get();
        //查询厂商用户信息
        log.info(String.valueOf(userInfo));
        PlatformAdminPO platformAdmin=platformAdminMapper.selectById(userInfo.getUserId());
        //对象映射
        AdminInfoVO adminInfoVO=mapStructMapper.adminToInfoVO(platformAdmin);
        return HttpResponseVO.<AdminInfoVO>builder()
                .data(adminInfoVO)
                .code(HttpStatusConstants.SUCCESS)
                .msg("账号信息查询成功")
                .build();
    }

    /**
     * 修改账号状态
     */
    @Override
    public HttpResponseVO<String> updateAdminInfo(UpdateAdminDTO updateVendorUserDTO) {
        //判断用户名是否存在
        if(updateVendorUserDTO.getUsername()!=null){
            CountIsExistDTO countIsExistDTO=new CountIsExistDTO();
            countIsExistDTO.setUsername(updateVendorUserDTO.getUsername());
            if(commonService.isExistAdmin(countIsExistDTO)||commonService.isExistVendorUser(countIsExistDTO)){
                return HttpResponseVO.<String>builder()
                        .code(HttpStatusConstants.ERROR)
                        .msg("用户名重复")
                        .build();
            }
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
                    .code(HttpStatusConstants.SUCCESS)
                    .msg("账号信息修改成功")
                    .build();
        }else{
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("更新失败，用户不存在或数据库发生变化")
                    .build();
        }
    }

    /**
     * 更换头像
     */
    @Override
    public HttpResponseVO<String> updateAvatar(MultipartFile file) {
        PcUserInfo userInfo = UserContext.get();
        PlatformAdminPO admin = platformAdminMapper.selectById(userInfo.getUserId());

        // 上传新头像
        String newAvatarUrl = fileStorageService.uploadFile(file, "avatar");

        // 删除旧头像
        String oldAvatarUrl = admin.getAvatar();
        if (oldAvatarUrl != null && !oldAvatarUrl.isBlank()) {
            fileStorageService.deleteFile(oldAvatarUrl);
        }

        // 更新数据库
        LambdaUpdateWrapper<PlatformAdminPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PlatformAdminPO::getAdminId, userInfo.getUserId());
        wrapper.set(PlatformAdminPO::getAvatar, newAvatarUrl);
        platformAdminMapper.update(null, wrapper);

        return HttpResponseVO.<String>builder()
                .code(HttpStatusConstants.SUCCESS)
                .msg("头像更换成功")
                .data(newAvatarUrl)
                .build();
    }

}

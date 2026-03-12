package org.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.audit.enums.VendorStatusEnum;
import org.example.audit.mapper.VendorMapper;
import org.example.audit.po.VendorPO;
import org.example.audit.service.FileStorageService;
import org.example.auth.common.PcUserInfo;
import org.example.auth.common.UserContext;
import org.example.auth.constants.Constants;
import org.example.auth.constants.HttpStatusConstants;
import org.example.auth.dto.CountIsExistDTO;
import org.example.auth.dto.UpdateVendorUserDTO;
import org.example.auth.mapper.MapStructMapper;
import org.example.auth.mapper.VendorUserMapper;
import org.example.auth.po.VendorUserPO;
import org.example.auth.service.CommonService;
import org.example.auth.service.VendorUserInfoService;
import org.example.auth.util.JsonUtil;
import org.example.auth.vo.HttpResponseVO;
import org.example.auth.vo.VendorSimpleVO;
import org.example.auth.vo.VendorUserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private VendorMapper vendorMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private FileStorageService fileStorageService;

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
                .code(HttpStatusConstants.SUCCESS)
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
                    .code(HttpStatusConstants.ERROR)
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
     * 获取当前厂商用户的已审核通过厂商列表
     */
    @Override
    public HttpResponseVO<List<VendorSimpleVO>> getApprovedVendors() {
        PcUserInfo userInfo = UserContext.get();
        List<Integer> vendorIds = userInfo.getVendorIds();

        List<VendorSimpleVO> result = new ArrayList<>();
        if (vendorIds != null && !vendorIds.isEmpty()) {
            LambdaQueryWrapper<VendorPO> wrapper = Wrappers.lambdaQuery();
            wrapper.in(VendorPO::getVendorId, vendorIds);
            wrapper.eq(VendorPO::getStatus, VendorStatusEnum.APPROVED);
            List<VendorPO> vendors = vendorMapper.selectList(wrapper);
            for (VendorPO vendor : vendors) {
                VendorSimpleVO vo = new VendorSimpleVO();
                vo.setVendorId(vendor.getVendorId());
                vo.setCompanyName(vendor.getCompanyName());
                vo.setShortName(vendor.getShortName());
                vo.setIsCurrent(vendor.getVendorId().equals(userInfo.getVendorId()));
                result.add(vo);
            }
        }

        return HttpResponseVO.<List<VendorSimpleVO>>builder()
                .data(result)
                .code(HttpStatusConstants.SUCCESS)
                .msg("查询成功")
                .build();
    }

    /**
     * 切换当前厂商
     */
    @Override
    public HttpResponseVO<String> switchVendor(Integer vendorId, String token) {
        PcUserInfo userInfo = UserContext.get();

        //校验vendorId是否在用户的厂商列表中
        if (userInfo.getVendorIds() == null || !userInfo.getVendorIds().contains(vendorId)) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("无权切换到该厂商")
                    .build();
        }

        //更新当前厂商
        userInfo.setVendorId(vendorId);

        //更新Redis中的用户信息
        redisTemplate.opsForValue().set(
                Constants.TOKEN_PREFIX + token,
                JsonUtil.toString(userInfo),
                Constants.TOKEN_EXPIRE_TIME,
                TimeUnit.SECONDS
        );

        //同步更新线程上下文
        UserContext.set(userInfo);

        return HttpResponseVO.<String>builder()
                .code(HttpStatusConstants.SUCCESS)
                .msg("切换厂商成功")
                .build();
    }

    /**
     * 更换头像
     */
    @Override
    public HttpResponseVO<String> updateAvatar(MultipartFile file) {
        PcUserInfo userInfo = UserContext.get();
        VendorUserPO vendorUser = vendorUserMapper.selectById(userInfo.getUserId());

        // 上传新头像
        String newAvatarUrl = fileStorageService.uploadFile(file, "avatar");

        // 删除旧头像
        String oldAvatarUrl = vendorUser.getAvatar();
        if (oldAvatarUrl != null && !oldAvatarUrl.isBlank()) {
            fileStorageService.deleteFile(oldAvatarUrl);
        }

        // 更新数据库
        LambdaUpdateWrapper<VendorUserPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(VendorUserPO::getVendorUserId, userInfo.getUserId());
        wrapper.set(VendorUserPO::getAvatar, newAvatarUrl);
        vendorUserMapper.update(null, wrapper);

        return HttpResponseVO.<String>builder()
                .code(HttpStatusConstants.SUCCESS)
                .msg("头像更换成功")
                .data(newAvatarUrl)
                .build();
    }

}

package org.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.auth.common.PasswordAndPhone;
import org.example.auth.common.PcUserInfo;
import org.example.auth.common.UserContext;
import org.example.auth.constants.Constants;
import org.example.auth.dto.*;
import org.example.auth.enums.PcUserIdentityEnum;
import org.example.auth.mapper.PlatformAdminMapper;
import org.example.auth.mapper.VendorUserMapper;
import org.example.auth.po.PlatformAdminPO;
import org.example.auth.po.VendorUserPO;
import org.example.auth.service.CommonService;
import org.example.auth.service.LoginService;
import org.example.auth.util.JsonUtil;
import org.example.auth.util.TokenUtil;
import org.example.auth.vo.AccessTokenVO;
import org.example.auth.vo.HttpResponseVO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author fasonghao
 */
@Service
public class LoginServiceImpl implements LoginService {
    private static final Logger logger = LoggerFactory.getLogger(LoginServiceImpl.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private PlatformAdminMapper platformAdminMapper;

    @Autowired
    private VendorUserMapper vendorUserMapper;

    @Autowired
    private CommonService commonService;

    /**
     * 账密登录
     * @param loginByPasswordDTO
     * @return
     */
    @Override
    public HttpResponseVO<AccessTokenVO> loginByPassword(LoginByPasswordDTO loginByPasswordDTO) {
        PcUserInfo pcUserInfo;
        //在厂商用户表中查找
        LambdaQueryWrapper<VendorUserPO> wrapperVendor= Wrappers.lambdaQuery();
        wrapperVendor.eq(VendorUserPO::getUsername,loginByPasswordDTO.getUsername());
        wrapperVendor.eq(VendorUserPO::getPassword,loginByPasswordDTO.getPassword());
        VendorUserPO vendorUser =vendorUserMapper.selectOne(wrapperVendor);

        if(vendorUser==null){
            //如果找不到，在平台管理员表中查找
            LambdaQueryWrapper<PlatformAdminPO> wrapperAdmin= Wrappers.lambdaQuery();
            wrapperAdmin.eq(PlatformAdminPO::getUsername,loginByPasswordDTO.getUsername());
            wrapperAdmin.eq(PlatformAdminPO::getPassword,loginByPasswordDTO.getPassword());
            PlatformAdminPO platformAdmin=platformAdminMapper.selectOne(wrapperAdmin);
            if(platformAdmin==null){
                //平台管理员表中也找不到，返回错误
                return HttpResponseVO.<AccessTokenVO>builder()
                        .code(HttpStatus.UNAUTHORIZED.value())
                        .msg("用户名或密码错误")
                        .build();
            }else{
                pcUserInfo=buildAdminInfo(platformAdmin);
            }
        }else{
            pcUserInfo=buildVendorInfo(vendorUser);
        }
        //生成token
        String token= TokenUtil.generateRandomToken();
        //存入redis
        redisTemplate.opsForValue().set(Constants.TOKEN_PREFIX+token, JsonUtil.toString(pcUserInfo), Constants.TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);

        return HttpResponseVO.<AccessTokenVO>builder()
                .data(new AccessTokenVO(token))
                .code(HttpStatus.OK.value())
                .msg("登录成功")
                .build();
    }

    /**
     * 短信验证码登录
     * @param loginBySmsCodeDTO
     * @return
     */
    @Override
    public HttpResponseVO<AccessTokenVO> loginBySmsCode(LoginBySmsCodeDTO loginBySmsCodeDTO) {
        String smsCode=redisTemplate.opsForValue().get(Constants.LOGIN_SMS_CODE_PREFIX+loginBySmsCodeDTO.getPhone());
        //判断验证码是否正确
        if(smsCode==null||!smsCode.equals(loginBySmsCodeDTO.getCode())){
            return HttpResponseVO.<AccessTokenVO>builder()
                    .code(HttpStatus.UNAUTHORIZED.value())
                    .msg("验证码错误或验证码已失效")
                    .build();
        }

        PcUserInfo pcUserInfo;
        //在厂商用户表中查找
        LambdaQueryWrapper<VendorUserPO> wrapperVendor= Wrappers.lambdaQuery();
        wrapperVendor.eq(VendorUserPO::getPhone,loginBySmsCodeDTO.getPhone());
        VendorUserPO vendorUser =vendorUserMapper.selectOne(wrapperVendor);

        if(vendorUser==null){
            //如果找不到，在平台管理员表中查找
            LambdaQueryWrapper<PlatformAdminPO> wrapperAdmin= Wrappers.lambdaQuery();
            wrapperAdmin.eq(PlatformAdminPO::getPhone,loginBySmsCodeDTO.getPhone());
            PlatformAdminPO platformAdmin=platformAdminMapper.selectOne(wrapperAdmin);

            if(platformAdmin==null){
                //平台管理员表中也找不到，返回错误
                return HttpResponseVO.<AccessTokenVO>builder()
                        .code(HttpStatus.UNAUTHORIZED.value())
                        .msg("用户不存在")
                        .build();
            }else{
                pcUserInfo=buildAdminInfo(platformAdmin);
            }
        }else{
            pcUserInfo=buildVendorInfo(vendorUser);
        }
        //生成token
        String token= TokenUtil.generateRandomToken();
        //存入redis
        redisTemplate.opsForValue().set(Constants.TOKEN_PREFIX+token, JsonUtil.toString(pcUserInfo), Constants.TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);

        return HttpResponseVO.<AccessTokenVO>builder()
                .data(new AccessTokenVO(token))
                .code(HttpStatus.OK.value())
                .msg("登录成功")
                .build();
    }

    /**
     * 根据平台管理员查询结果封装信息
     */
    private PcUserInfo buildAdminInfo(PlatformAdminPO platformAdmin){
        PcUserInfo pcUserInfo=new PcUserInfo();
        //根据管理员是否为超级管理员封装信息
        pcUserInfo.setUserId(platformAdmin.getAdminId());
        if(platformAdmin.getIsSuperAdmin()){
            pcUserInfo.setRole(PcUserIdentityEnum.SUPER_ADMIN);
        }else{
            pcUserInfo.setRole(PcUserIdentityEnum.ORDINARY_ADMIN);
            //查询普通平台管理员权限
            //pcUserInfo.setPermissions();
        }
        return pcUserInfo;
    }

    /**
     * 根据厂商用户查询结果封装信息
     */
    private PcUserInfo buildVendorInfo(VendorUserPO vendorUser){
        PcUserInfo pcUserInfo=new PcUserInfo();
        //封装厂商用户的信息
        pcUserInfo.setUserId(vendorUser.getVendorUserId());
        pcUserInfo.setRole(PcUserIdentityEnum.VENDOR_USER);
        //查询该厂商用户关联的厂商列表
        //pcUserInfo.setVendorIds();
        return pcUserInfo;
    }

    /**
     * 发送短信验证码
     */
    @Override
    public HttpResponseVO<String> sendSmsCode(SendSmsCodeDTO sendSmsCodeDTO) {
        String key=sendSmsCodeDTO.getPurpose().getName()+ sendSmsCodeDTO.getPhone();
        //检查redis中是否存有手机号对应的验证码
        if((Constants.SMS_CODE_EXPIRE_TIME-redisTemplate.getExpire(key))<Constants.SMS_CODE_SPAN){
            //如果有，且时间间隔小于两次发送的间隔，拒绝发送
            return HttpResponseVO.<String>builder()
                    .code(HttpStatus.TOO_MANY_REQUESTS.value())
                    .msg("对同一手机号发送短信验证码间隔不得少于1分钟")
                    .build();
        }

        //随机生成短信验证码
        Random random = new Random();
        String code = String.format("%06d",random.nextInt(1000000));

        Map<String, String> map = new HashMap<>();
        map.put("name", Constants.SMS_CODE_NAME);
        map.put("code", code);
        map.put("number", String.valueOf(Constants.SMS_CODE_EXPIRE_TIME/60));
        map.put("to", sendSmsCodeDTO.getPhone());

        HttpURLConnection conn = null;

        try {
            URL url = new URL(Constants.SMS_CODE_URL);
            conn = (HttpURLConnection) url.openConnection();

            //设置连接参数
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            //将请求参数写入到请求体中
            try (OutputStream os = conn.getOutputStream()) {
                String jsonParam = JsonUtil.toString(map);
                os.write(jsonParam.getBytes("UTF-8"));
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                logger.error("发送短信验证码失败，响应码：{}，手机号：{}", responseCode, sendSmsCodeDTO.getPhone());
                return HttpResponseVO.<String>builder()
                        .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .msg("验证码发送失败，请稍后重试")
                        .build();
            }

            StringBuilder responseStr = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    responseStr.append(line);
                }
            }


            //存入redis中
            redisTemplate.opsForValue().set(key, code, Constants.SMS_CODE_EXPIRE_TIME, TimeUnit.SECONDS);

            logger.info("发送短信验证码成功，手机号：{}，响应内容：{}", sendSmsCodeDTO.getPhone(), responseStr);
            return HttpResponseVO.<String>builder()
                    .code(HttpStatus.OK.value())
                    .msg("验证码发送成功，请注意查收")
                    .build();

        } catch (IOException e) {
            logger.error("发送短信验证码IO异常，手机号：{}", sendSmsCodeDTO.getPhone(), e);
            return HttpResponseVO.<String>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .msg("验证码发送失败，网络异常")
                    .build();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * 修改账号密码
     */
    @Override
    public HttpResponseVO<String> updatePassword(UpdatePasswordDTO updatePasswordDTO) {
        //获取用户的手机号码
        PcUserInfo userInfo = UserContext.get();
        String phone=getPhone(userInfo);

        //确认验证码是否正确
        String smsCode=redisTemplate.opsForValue().get(Constants.UPDATE_PASSWORD_SMS_CODE_PREFIX+phone);
        if(smsCode==null||!smsCode.equals(updatePasswordDTO.getCode())){
            //不正确，返回错误
            return HttpResponseVO.<String>builder()
                    .code(HttpStatus.UNAUTHORIZED.value())
                    .msg("验证码错误或验证码已失效")
                    .build();
        }

        //正确，修改数据库中的密码
        PasswordAndPhone passwordAndPhone=new PasswordAndPhone();
        passwordAndPhone.setPassword(updatePasswordDTO.getPassword());

        return updatePasswordOrPhone(userInfo,passwordAndPhone);
    }

    /**
     * 确认原手机号的验证码
     */
    @Override
    public HttpResponseVO<String> confirmOldPhone(ConfirmOldPhoneDTO confirmOldPhoneDTO) {
        //获取用户的手机号码
        PcUserInfo userInfo = UserContext.get();
        String phone=getPhone(userInfo);

        //确认验证码是否正确
        String smsCode=redisTemplate.opsForValue().get(Constants.UPDATE_PHONE_SMS_CODE_PREFIX+phone);
        if(smsCode==null||!smsCode.equals(confirmOldPhoneDTO.getCode())){
            //不正确，返回401
            return HttpResponseVO.<String>builder()
                    .code(HttpStatus.UNAUTHORIZED.value())
                    .msg("验证码错误或验证码已失效")
                    .build();
        }

        return HttpResponseVO.<String>builder()
                .code(HttpStatus.OK.value())
                .msg("验证码正确")
                .build();
    }

    /**
     * 更换绑定的手机号
     */
    @Override
    public HttpResponseVO<String> updatePhone(UpdatePhoneDTO updatePhoneDTO) {
        //判断新手机号是否重复
        CountIsExistDTO countIsExistDTO=new CountIsExistDTO();
        countIsExistDTO.setPhone(updatePhoneDTO.getPhone());
        if(commonService.isExistAdmin(countIsExistDTO)|| commonService.isExistVendorUser(countIsExistDTO)){
            return HttpResponseVO.<String>builder()
                    .code(HttpStatus.CONFLICT.value())
                    .msg("该手机号已绑定其他账号")
                    .build();
        }

        //获取用户的手机号码
        PcUserInfo userInfo = UserContext.get();
        String phone=getPhone(userInfo);

        //判断原手机号验证码是否正确
        String oldSmsCode=redisTemplate.opsForValue().get(Constants.UPDATE_PHONE_SMS_CODE_PREFIX+phone);
        if(oldSmsCode==null||!oldSmsCode.equals(updatePhoneDTO.getOldCode())){
            //不正确，返回401
            return HttpResponseVO.<String>builder()
                    .data("old")
                    .code(HttpStatus.UNAUTHORIZED.value())
                    .msg("操作时间过长，请重新操作")
                    .build();
        }

        //新手机号验证码是否正确
        String newSmsCode=redisTemplate.opsForValue().get(Constants.UPDATE_PHONE_SMS_CODE_PREFIX+updatePhoneDTO.getPhone());
        if(newSmsCode==null||!newSmsCode.equals(updatePhoneDTO.getNewCode())){
            //不正确，返回401
            return HttpResponseVO.<String>builder()
                    .data("new")
                    .code(HttpStatus.UNAUTHORIZED.value())
                    .msg("验证码错误或验证码已失效")
                    .build();
        }

        //正确，修改数据库中的手机号
        PasswordAndPhone passwordAndPhone=new PasswordAndPhone();
        passwordAndPhone.setPhone(updatePhoneDTO.getPhone());

        return updatePasswordOrPhone(userInfo,passwordAndPhone);
    }

    /**
     * 获取用户数据库中的手机号
     */
    private String getPhone(PcUserInfo userInfo){
        if(userInfo.getRole()==PcUserIdentityEnum.VENDOR_USER){
            VendorUserPO vendorUser=vendorUserMapper.selectById(userInfo.getUserId());
            return vendorUser.getPhone();
        }else{
            PlatformAdminPO platformAdmin=platformAdminMapper.selectById(userInfo.getUserId());
            return platformAdmin.getPhone();
        }
    }

    /**
     * 执行密码或手机号的数据库更新操作
     */
    private HttpResponseVO<String> updatePasswordOrPhone(PcUserInfo userInfo, PasswordAndPhone passwordAndPhone){
        int code;

        if(userInfo.getRole()==PcUserIdentityEnum.VENDOR_USER){
            LambdaUpdateWrapper<VendorUserPO> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(VendorUserPO::getVendorUserId, userInfo.getUserId());

            //动态设置需要更新的字段
            if (passwordAndPhone.getPassword() != null) {
                wrapper.set(VendorUserPO::getPassword, passwordAndPhone.getPassword());
            }
            if (passwordAndPhone.getPhone() != null) {
                wrapper.set(VendorUserPO::getPhone, passwordAndPhone.getPhone());
            }

            code=vendorUserMapper.update(null, wrapper);
        }else{
            LambdaUpdateWrapper<PlatformAdminPO> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(PlatformAdminPO::getAdminId, userInfo.getUserId());

            if (passwordAndPhone.getPassword() != null) {
                wrapper.set(PlatformAdminPO::getPassword, passwordAndPhone.getPassword());
            }
            if (passwordAndPhone.getPhone() != null) {
                wrapper.set(PlatformAdminPO::getPhone, passwordAndPhone.getPhone());
            }

            code=platformAdminMapper.update(null, wrapper);

        }

        if (code>0) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatus.OK.value())
                    .msg("修改成功")
                    .build();
        }else{
            return HttpResponseVO.<String>builder()
                    .code(HttpStatus.NOT_FOUND.value())
                    .msg("修改失败，用户不存在或数据库发生变化")
                    .build();
        }
    }
}

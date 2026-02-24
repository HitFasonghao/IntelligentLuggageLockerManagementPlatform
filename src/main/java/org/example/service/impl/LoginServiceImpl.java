package org.example.service.impl;

import org.example.constants.Constants;
import org.example.dto.SmsCodeDTO;
import org.example.service.LoginService;
import org.example.util.JsonUtil;
import org.example.vo.HttpResponseVO;

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

    @Override
    public HttpResponseVO<String> loginByPassword() {
        return null;
    }

    @Override
    public HttpResponseVO<String> loginBySmsCode() {
        return null;
    }

    /**
     * 发送短信验证码
     * @param smsCodeDTO
     * @return
     */
    @Override
    public HttpResponseVO<String> sendSmsCode(SmsCodeDTO smsCodeDTO) {
        Random random = new Random();
        String code = String.format("%06d",random.nextInt(1000000));

        Map<String, String> map = new HashMap<>();
        map.put("name", Constants.SMS_CODE_NAME);
        map.put("code", code);
        map.put("number", String.valueOf(Constants.SMS_CODE_EXPIRE_TIME));
        map.put("to", smsCodeDTO.getPhone());

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
                logger.error("发送短信验证码失败，响应码：{}，手机号：{}", responseCode, smsCodeDTO.getPhone());
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
            String key=Constants.SMS_CODE_PREFIX+smsCodeDTO.getPhone();
            redisTemplate.opsForValue().set(key, code);
            redisTemplate.expire(key,Constants.SMS_CODE_EXPIRE_TIME, TimeUnit.MINUTES);

            logger.info("发送短信验证码成功，手机号：{}，响应内容：{}", smsCodeDTO.getPhone(), responseStr);
            return HttpResponseVO.<String>builder()
                    .code(HttpStatus.OK.value())
                    .msg("验证码发送成功，请注意查收")
                    .build();

        } catch (IOException e) {
            logger.error("发送短信验证码IO异常，手机号：{}", smsCodeDTO.getPhone(), e);
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
}

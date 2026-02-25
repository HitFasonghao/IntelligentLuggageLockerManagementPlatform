package org.example.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.common.PcUserInfo;
import org.example.constants.Constants;
import org.example.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;


/**
 * 管理员拦截器
 *
 * @author fasonghao
 */
@Component
public class PcUserInterceptor implements HandlerInterceptor {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //设置响应编码
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        //从请求头获取Authorization
        String token = request.getHeader("Authorization");

        //验证Authorization是否存在且格式正确
        if (token == null || token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":401,\"message\":\"未提供认证令牌\"}");
            return false;
        }

        //在Redis中查找token对应的用户信息
        String userInfoJson = redisTemplate.opsForValue().get(Constants.TOKEN_PREFIX + token);

        if (userInfoJson == null) {
            //Redis中找不到token，说明未登录或token已过期
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":401,\"message\":\"认证已过期，请重新登录\"}");
            return false;
        }

        //将用户信息存入request属性中，供后续业务使用
        PcUserInfo pcUserInfo= JsonUtil.toObject(userInfoJson, PcUserInfo.class);
        request.setAttribute("pcUserInfo", pcUserInfo);

        //续期token（每次请求都重新设置过期时间）
        redisTemplate.expire(Constants.TOKEN_PREFIX + token, Constants.TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);

        return true;
    }
}

package org.example.config;

import org.example.interceptor.PcUserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author fasonghao
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private PcUserInterceptor pcUserInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(pcUserInterceptor)
                .addPathPatterns("/user/**",
                        "/vendorUser/**",
                        "/admin/**",
                        "/login/**")
                .excludePathPatterns("/login/smsCode",
                        "/login/loginByPassword",
                        "/login/loginBySmsCode");
    }
}

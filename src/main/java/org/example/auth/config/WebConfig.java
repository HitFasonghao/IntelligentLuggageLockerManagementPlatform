package org.example.auth.config;

import org.example.auth.interceptor.PcUserInterceptor;
import org.example.device.config.EnumConverterFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author fasonghao
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private PcUserInterceptor pcUserInterceptor;

    @Autowired
    private EnumConverterFactory enumConverterFactory;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(enumConverterFactory);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(pcUserInterceptor)
                .addPathPatterns("/user/**",
                        "/vendorUser/**",
                        "/admin/**",
                        "/login/**",
                        "/permission/**",
                        "/vendor/**",
                        "/audit/**",
                        "/vendor-mgmt/**",
                        "/cabinet/**",
                        "/cabinetKind/**",
                        "/cluster/**")
                .excludePathPatterns("/login/smsCode",
                        "/login/loginByPassword",
                        "/login/loginBySmsCode",
                        "/login/captcha");
    }
}

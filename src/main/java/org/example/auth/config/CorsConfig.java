package org.example.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置
 * @author fasonghao
 */
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许的源（前端域名），* 表示允许所有源（开发环境可用，生产环境建议指定具体域名）
        config.addAllowedOrigin("http://localhost:3200");
        // 允许携带 Cookie
        config.setAllowCredentials(true);
        // 允许的请求方法（GET/POST/PUT/DELETE 等）
        config.addAllowedMethod("*");
        // 允许的请求头（* 表示所有）
        config.addAllowedHeader("*");
        // 暴露的响应头（前端能获取的自定义头）
        config.addExposedHeader("Authorization");

        // 配置生效的 URL 路径（所有接口）
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
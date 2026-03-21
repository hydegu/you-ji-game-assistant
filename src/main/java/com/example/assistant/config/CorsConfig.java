package com.example.assistant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * CORS跨域配置
 * 允许前端(Vue)跨域访问后端API
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 允许的前端地址(开发环境)
        config.addAllowedOriginPattern("*");  // 允许所有来源(仅限开发环境)

        // 允许所有HTTP方法
        config.addAllowedMethod("*");

        // 允许所有请求头
        config.addAllowedHeader("*");

        // 允许携带凭证(Cookie、Authorization等)
        config.setAllowCredentials(true);

        // 预检请求的有效期(1小时)
        config.setMaxAge(3600L);

        // 暴露的响应头(前端可以访问的响应头)
        config.addExposedHeader("Authorization");
        config.addExposedHeader("authentication");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}


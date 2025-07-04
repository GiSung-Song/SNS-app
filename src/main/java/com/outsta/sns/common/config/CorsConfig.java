package com.outsta.sns.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS 설정 클래스
 */
@Configuration
public class CorsConfig {

    /**
     * CORS 정책
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 허용할 Origin 설정 (프론트 개발 환경 도메인)
        config.setAllowedOrigins(List.of("http://localhost:3000"));

        // 허용할 HTTP 메서드
        config.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE"));

        // 모든 요청 헤더 적용
        config.setAllowedHeaders(List.of("*"));

        // 인증 정보 포함 허용 (쿠키 등)
        config.setAllowCredentials(true);

        // /api/** 경로에 CORS 설정 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);

        return source;
    }

}

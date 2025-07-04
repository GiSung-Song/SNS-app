package com.outsta.sns.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger 설정 클래스
 */
@Configuration
public class SwaggerConfig {

    /** Swagger UI에 표시할 JWT 인증 스키마 이름 */
    private static final String SECURITY_SCHEME_NAME = "JWT Token";

    /**
     * OpenAPI Bean 등록
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OUTSTA SNS API")
                        .description("OUTSTA SNS API 명세서")
                        .version("v1.0.0"))
                // JWT 토큰 인증 추가
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))

                // JWT 인증 스키마 설정 (Bearer 방식)
                .components(new Components().addSecuritySchemes(
                        SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                ));
    }
}

package com.outsta.sns.common.config.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security 설정 (개발 환경)
 */
@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
@Profile("dev")
public class SecurityConfig {

    /** JWT 필터 */
    private final JwtFilter jwtFilter;

    /** CORS 설정 */
    private final CorsConfigurationSource corsConfigurationSource;

    /**
     * 비밀번호 암호화에 사용할 BCrypt 인코더
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Spring Security 필터 체인
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .authorizeHttpRequests((auth) -> auth
                        // Swagger 문서 경로 전체 허용
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // 로그인 및 토큰 재발급 경로 전체 허용
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/reissue").permitAll()

                        // 회원가입 및 중복체크(이메일, 닉네임) 경로 전체 허용
                        .requestMatchers(HttpMethod.POST, "/api/members").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/members/check-email").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/members/check-nickname").permitAll()

                        // TODO: 추후 변경 예정
                        .anyRequest().permitAll()
                )
                // CORS 설정
                .cors(cors -> cors
                        .configurationSource(corsConfigurationSource))

                // CSRF 비활성화
                .csrf(CsrfConfigurer::disable)

                // HTTP Basic 인증 비활성화
                .httpBasic(HttpBasicConfigurer::disable)

                // Form 로그인 비활성화
                .formLogin(FormLoginConfigurer::disable)

                // 세션 사용하지 않음 (JWT 인증 방식)
                .sessionManagement((session) ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 인증 실패 시 401 반환
                .exceptionHandling((exception) ->
                        exception.authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
                        })
                )

                // JWT 필터 추가
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}

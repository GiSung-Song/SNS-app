package com.outsta.sns.common.config.security;

import org.springframework.http.HttpMethod;

/**
 * 인증 예외 경로 DTO
 * - 특정 HTTP 메서드 + URI 경로를 지정
 *
 * @param method     허용할 HTTP 메서드
 * @param pathPrefix 허용할 경로 Prefix
 */
public record PermitPass(HttpMethod method, String pathPrefix) {

    /**
     * 현재 요청의 메서드와 경로가 일치하는지 확인
     *
     * @param reqMethod 요청 HTTP 메서드
     * @param reqPath   요청 URI 경로
     * @return 메서드와 경로가 일치하면 true, 아니면 false
     */
    public boolean matches(String reqMethod, String reqPath) {
        return (method == null || method.name().equalsIgnoreCase(reqMethod)) && reqPath.startsWith(pathPrefix);
    }
}

package com.outsta.sns.common.config.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JWT 토큰에 담을 사용자 정보
 * - 회원 ID, 이메일, Role
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JwtPayload {
    /** 회원 식별자 ID */
    private Long id;

    /** 회원 이메일 */
    private String email;

    /** 회원 권한 역할 (GUEST, MEMBER, ADMIN) */
    private String role;
}
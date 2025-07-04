package com.outsta.sns.common.config.security;

import com.outsta.sns.domain.enums.Role;

/**
 * Spring Security 인증 객체의 사용자 정보 DTO
 *
 * @param id    사용자 식별자 ID
 * @param role  사용자 Role
 * @param email 사용자 이메일
 */
public record CustomUserDetails(Long id, Role role, String email) {
}
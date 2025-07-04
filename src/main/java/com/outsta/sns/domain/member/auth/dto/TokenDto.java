package com.outsta.sns.domain.member.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 토큰 Response DTO
 * - Access Token, Refresh Token
 */
@Schema(description = "토큰 Response DTO")
public record TokenDto(

        /** JWT 액세스 토큰 */
        @Schema(name = "Access Token")
        String accessToken,

        /** JWT 리프레시 토큰 */
        @Schema(name = "Refresh Token")
        String refreshToken
) { }

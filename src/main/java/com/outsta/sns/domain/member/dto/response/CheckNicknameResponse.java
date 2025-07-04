package com.outsta.sns.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 닉네임 중복 여부 Response DTO
 * - 중복여부
 * 중복이면 true, 아니면 false
 */
@Schema(description = "닉네임 중복 여부 Response DTO")
public record CheckNicknameResponse(
        @Schema(description = "중복 여부") boolean duplicated) {
}
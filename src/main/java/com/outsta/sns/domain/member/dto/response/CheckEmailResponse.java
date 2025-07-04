package com.outsta.sns.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 이메일 중복 여부 Response DTO
 * - 중복여부
 * 중복이면 true, 아니면 false
 */
@Schema(description = "이메일 중복 여부 Response DTO")
public record CheckEmailResponse(
        @Schema(description = "중복 여부") boolean duplicated) {
}

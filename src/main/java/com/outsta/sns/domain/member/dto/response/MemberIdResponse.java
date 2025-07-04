package com.outsta.sns.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 회원 식별자 ID Response DTO
 * - 회원 식별자 ID
 */
@Schema(description = "회원 ID Response DTO")
public record MemberIdResponse(
        @Schema(description = "회원 고유 ID") Long memberId) {
}
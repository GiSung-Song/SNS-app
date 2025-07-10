package com.outsta.sns.domain.block.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 차단 목록 Response DTO
 * - 회원 식별자 ID, 회원 닉네임
 */
@Schema(description = "차단 목록 Response DTO")
public record BlockListResponse(
        @Schema(description = "차단 목록") List<BlockMemberDto> blockedList
) {
    public record BlockMemberDto(
            @Schema(description = "회원 ID") Long memberId,
            @Schema(description = "닉네임") String nickname) {
    }
}
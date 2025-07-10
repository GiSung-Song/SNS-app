package com.outsta.sns.domain.follow.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 팔로워 목록 Response DTO
 * - 회원 식별자 ID, 회원 닉네임
 */
@Schema(description = "팔로워 목록 Response DTO")
public record FollowerListResponse(
        @Schema(description = "팔로워 목록") List<FollowerMemberDto> followerList
) {
    public record FollowerMemberDto(
            @Schema(description = "회원 ID") Long memberId,
            @Schema(description = "닉네임") String nickname) {
    }
}

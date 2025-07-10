package com.outsta.sns.domain.follow.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 팔로잉 목록 Response DTO
 * - 회원 식별자 ID, 회원 닉네임
 */
@Schema(description = "팔로잉 목록 Response DTO")
public record FollowingListResponse(
        @Schema(description = "팔로잉 목록") List<FollowingMemberDto> followingList
) {
    public record FollowingMemberDto(
            @Schema(description = "회원 ID") Long memberId,
            @Schema(description = "닉네임") String nickname) {
    }
}
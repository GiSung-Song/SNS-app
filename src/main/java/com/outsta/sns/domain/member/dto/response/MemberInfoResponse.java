package com.outsta.sns.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * 회원 정보 Response DTO
 */
@Schema(description = "회원 정보 Response DTO")
public record MemberInfoResponse(
        @Schema(description = "회원 고유 ID")
        Long id,

        @Schema(description = "이름")
        String name,

        @Schema(description = "닉네임")
        String nickname,

        @Schema(description = "생년월일")
        LocalDate birth,

        @Schema(description = "성별")
        String gender,

        @Schema(description = "성별 한글")
        String genderName,

        @Schema(description = "팔로워 수")
        long followerCount,

        @Schema(description = "팔로잉 수")
        long followingCount,

        @Schema(description = "프로필 이미지 고유 ID")
        Long profileImageId,

        @Schema(description = "프로필 이미지 URL")
        String imageUrl,

        @Schema(description = "원본 파일명")
        String originName,

        @Schema(description = "저장된 파일명")
        String fileName
) {
}

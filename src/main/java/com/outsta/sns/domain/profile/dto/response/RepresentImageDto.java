package com.outsta.sns.domain.profile.dto.response;

import com.outsta.sns.domain.profile.entity.ProfileImage;

/**
 * 대표 이미지 DTO
 * - 내부 로직용 DTO
 */
public record RepresentImageDto(
        /** 프로필 이미지 식별자 ID */
        Long profileImageId,

        /** 프로필 이미지 저장 경로 URL */
        String imageUrl,

        /** 원본 파일명 */
        String originName,

        /** 저장된 파일명 */
        String fileName
) {
    public static RepresentImageDto from(ProfileImage profileImage) {
        return new RepresentImageDto(
                profileImage.getId(),
                profileImage.getImageUrl(),
                profileImage.getOriginName(),
                profileImage.getFileName()
        );
    }
}
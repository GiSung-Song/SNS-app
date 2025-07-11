package com.outsta.sns.domain.profile.dto.response;

import com.outsta.sns.domain.profile.entity.ProfileImage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

import java.util.List;

/**
 * 프로필 이미지  Response DTO
 * - 프로필 이미지 URL, 원본 파일명, 저장된 파일명, 대표 이미지 여부
 */
@Schema(description = "프로필 이미지 Response DTO")
public record ProfileImageResponse(
        /** 프로필 이미지 목록 */
        @Schema(description = "프로필 이미지 목록")
        List<ProfileImageDto> profileImageList
) {
    public record ProfileImageDto(
            /** 프로필 이미지 식별자 ID */
            @Schema(description = "프로필 이미지 식별자 ID")
            Long profileImageId,

            /** 프로필 이미지 저장 경로 URL */
            @Schema(description = "프로필 이미지 저장 경로")
            String imageUrl,

            /** 원본 파일명 */
            @Schema(description = "원본 파일명")
            String originName,

            /** 저장된 파일명 */
            @Schema(description = "저장된 파일명")
            String fileName,

            /** 대표 이미지 여부 */
            @Schema(description = "대표 이미지 여부")
            boolean represent
    ) {
        public static ProfileImageDto from(ProfileImage profileImage) {
            return new ProfileImageDto(
                    profileImage.getId(),
                    profileImage.getImageUrl(),
                    profileImage.getOriginName(),
                    profileImage.getFileName(),
                    profileImage.isRepresent()
            );
        }
    }
}

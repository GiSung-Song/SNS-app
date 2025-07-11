package com.outsta.sns.domain.profile.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

/**
 * 프로필 이미지 등록 Request DTO
 * - 프로필 이미지 URL, 원본 파일명, 저장된 파일명, 대표 이미지 여부
 */
@Schema(description = "프로필 이미지 등록 Request DTO")
public record ProfileImageRequest(

        /** 프로필 이미지 저장 경로 URL */
        @Schema(description = "프로필 이미지 저장 경로", example = "https://cloud.storage.com/profile/uuid-250601.jpg", maxLength = 300)
        @NotBlank(message = "프로필 이미지 경로는 필수입니다.")
        @Length(max = 300, message = "프로필 이미지 경로는 최대 300자 입니다.")
        String imageUrl,

        /** 원본 파일명 */
        @Schema(description = "원본 파일명", example = "사진.jpg", maxLength = 100)
        @NotBlank(message = "원본 파일명은 필수입니다.")
        @Length(max = 100, message = "원본 파일명은 최대 100자 입니다.")
        String originName,

        /** 저장된 파일명 */
        @Schema(description = "저장된 파일명", example = "uuid-250601.jpg", maxLength = 100)
        @NotBlank(message = "저장된 파일명은 필수입니다.")
        @Length(max = 100, message = "저장된 파일명은 최대 100자 입니다.")
        String fileName,

        /** 대표 이미지 여부 */
        @Schema(description = "대표 이미지 여부", example = "true")
        boolean represent
) {
}

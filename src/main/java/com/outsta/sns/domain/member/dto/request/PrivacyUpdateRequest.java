package com.outsta.sns.domain.member.dto.request;

import com.outsta.sns.common.validation.ValidEnum;
import com.outsta.sns.domain.enums.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 프로필 공개 범위 설정 Request DTO
 * - 공개 범위
 */
@Schema(description = "프로필 공개 범위 설정 Request DTO")
public record PrivacyUpdateRequest(

        /** 공개 범위 */
        @Schema(description = "정보 공개 범위", example = "FOLLOWER_ONLY")
        @NotBlank(message = "정보 공개 범위는 필수입니다.")
        @ValidEnum(enumClass = Visibility.class, message = "PUBLIC, FOLLOWER_ONLY, PRIVATE 중 입력해주세요.")
        String visibility
) {
}
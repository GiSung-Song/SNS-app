package com.outsta.sns.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

/**
 * 비밀번호 변경 Request DTO
 * - 비밀번호
 */
@Schema(description = "비밀번호 변경 Request DTO")
public record PasswordUpdateRequest(

        /** 변경할 새로운 비밀번호 */
        @Schema(description = "비밀번호", example = "password1313", minLength = 8, maxLength = 15)
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Length(min = 8, max = 15, message = "비밀번호는 8 ~ 15자 입니다.")
        String password
) {
}
package com.outsta.sns.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

/**
 * 이메일 중복 체크 Request DTO
 * - 이메일
 */
@Schema(description = "이메일 중복 체크 Request DTO")
public record EmailCheckRequest(

        /** 회원 이메일 */
        @Schema(description = "이메일", example = "tester@email.com", minLength = 7, maxLength = 50)
        @NotBlank(message = "이메일은 필수입니다.")
        @Length(min = 7, max = 50, message = "이메일은 7 ~ 50자 입니다.")
        @Email(
                regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
                message = "이메일 형식으로 작성해주세요."
        )
        String email
) {
}

package com.outsta.sns.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

/**
 * 인증 코드 체크 Request DTO
 * - 이메일, 인증 코드
 */
@Schema(description = "인증 코드 체크 Request DTO")
public record CodeCheckRequest(

        /** 회원 이메일 */
        @Schema(description = "이메일", example = "tester@email.com", minLength = 7, maxLength = 50)
        @NotBlank(message = "이메일은 필수입니다.")
        @Length(min = 7, max = 50, message = "이메일은 7 ~ 50자 입니다.")
        @Email(
                regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
                message = "이메일 형식으로 작성해주세요."
        )
        String email,

        /** 인증 코드 */
        @Schema(description = "인증 코드", example = "23idsijpAd", minLength = 10, maxLength = 10)
        @NotBlank(message = "인증 코드는 필수입니다.")
        @Length(min = 10, max = 10, message = "인증 코드는 10자 입니다.")
        String code
) {
}

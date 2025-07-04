package com.outsta.sns.domain.member.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

/**
 * 로그인 Request DTO
 * - 이메일, 비밀번호
 */
@Schema(description = "로그인 Request DTO")
public record LoginRequest(

        /** 회원 이메일 */
        @Schema(description = "이메일", example = "tester@email.com", minLength = 7, maxLength = 50)
        @NotBlank(message = "이메일은 필수입니다.")
        @Length(min = 7, max = 50, message = "이메일은 7 ~ 50자 입니다.")
        @Email(
                regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
                message = "이메일 형식으로 작성해주세요."
        )
        String email,

        /** 회원 비밀번호 */
        @Schema(description = "비밀번호", example = "password1313", minLength = 8, maxLength = 15)
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Length(min = 8, max = 15, message = "비밀번호는 8 ~ 15자 입니다.")
        String password
) { }
package com.outsta.sns.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

/**
 * 회원 탈퇴 취소 Request DTO
 * - 이름, 이메일, 비밀번호, 생년월일
 */
@Schema(description = "회원 탈퇴 취소 Request DTO")
public record CancelDeleteRequest(

        /** 회원 이름 */
        @Schema(description = "이름", example = "송기성", minLength = 2, maxLength = 50)
        @NotBlank(message = "이름은 필수입니다.")
        @Length(min = 2, max = 50, message = "이름은 2 ~ 50자 입니다.")
        String name,

        /** 비밀번호 */
        @Schema(description = "비밀번호", example = "password1313", minLength = 8, maxLength = 15)
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Length(min = 8, max = 15, message = "비밀번호는 8 ~ 15자 입니다.")
        String password,

        /** 회원 이메일 */
        @Schema(description = "이메일", example = "tester@email.com", minLength = 7, maxLength = 50)
        @NotBlank(message = "이메일은 필수입니다.")
        @Length(min = 7, max = 50, message = "이메일은 7 ~ 50자 입니다.")
        @Email(
                regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
                message = "이메일 형식으로 작성해주세요."
        )
        String email,

        /** 생년월일 */
        @Schema(description = "생일", example = "1980-01-27")
        @NotNull(message = "생일은 필수입니다.")
        LocalDate birth
) {
}

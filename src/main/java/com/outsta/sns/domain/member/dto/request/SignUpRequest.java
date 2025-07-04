package com.outsta.sns.domain.member.dto.request;

import com.outsta.sns.common.validation.ValidEnum;
import com.outsta.sns.domain.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

/**
 * 회원가입 Request DTO
 * - 이름, 비밀번호, 닉네임, 이메일, 생년월일, 성별
 */
@Schema(description = "회원가입 Request DTO")
public record SignUpRequest(

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

        /** 닉네임 */
        @Schema(description = "닉네임", example = "아브라카다브라", minLength = 2, maxLength = 30)
        @NotBlank(message = "닉네임은 필수입니다.")
        @Length(min = 2, max = 30, message = "닉네임은 2 ~ 30자 입니다.")
        String nickname,

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
        LocalDate birth,

        /** 성별 */
        @Schema(description = "성별", example = "MALE")
        @NotBlank(message = "성별은 필수입니다.")
        @ValidEnum(enumClass = Gender.class, message = "MALE, FEMALE 중 입력해주세요.")
        String gender
) {
}

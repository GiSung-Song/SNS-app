package com.outsta.sns.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "닉네임 중복 확인 Request DTO")
public class CheckNicknameRequest {

    @Schema(description = "닉네임", example = "아브라카다브라", minLength = 2, maxLength = 30)
    @NotBlank(message = "닉네임은 필수입니다.")
    @Length(min = 2, max = 30, message = "닉네임은 2 ~ 30자 입니다.")
    private String nickname;
}

package com.outsta.sns.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이메일 중복 여부 Response DTO")
public record CheckEmailResponse(
        @Schema(description = "중복 여부") boolean isDuplicated) {
}

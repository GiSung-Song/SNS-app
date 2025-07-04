package com.outsta.sns.common.error;

import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * API 에러 응답 DTO
 * @param status  HTTP 상태 코드
 * @param message 에러 메시지
 * @param errors  필드 단위 검증 오류 목록
 */
public record ErrorResponse(
        int status,
        String message,
        List<FieldError> errors
) {

    /**
     * 필드 단위 오류가 없는 에러 응답
     * @param status  HTTP 상태
     * @param message 에러 메시지
     * @return 에러 응답 객체
     */
    public static ErrorResponse of(HttpStatus status, String message) {
        return new ErrorResponse(status.value(), message, null);
    }

    /**
     * 필드 단위 오류가 있는 에러 응답
     * @param status  HTTP 상태
     * @param message 에러 메시지
     * @param errors  필드 오류 리스트
     * @return 에러 응답 객체
     */
    public static ErrorResponse of(HttpStatus status, String message, List<FieldError> errors) {
        return new ErrorResponse(status.value(), message, errors);
    }

    /**
     * 필드 단위 오류 정보 DTO
     * @param field  오류가 발생한 필드명
     * @param reason 오류가 발생한 이유
     */
    public record FieldError(String field, String reason) {}
}
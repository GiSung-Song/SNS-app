package com.outsta.sns.common.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * API 전역 예외 처리
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 커스텀 예외 처리
     * @param ex 커스텀 예외
     * @return ErrorResponse를 포함한 ResponseEntity
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(ErrorResponse.of(
                        errorCode.getHttpStatus(),
                        errorCode.getMessage()
                ));
    }

    /**
     * @Valid 검증 실패 시 발생하는 예외 처리
     * @param ex 검증 실패 예외
     * @return 필드별 오류 정보를 담은 ErrorResponse를 포함한 ResponseEntity
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<ErrorResponse.FieldError> fieldErrorList = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ErrorResponse.FieldError(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ErrorResponse.of(
                        HttpStatus.BAD_REQUEST,
                        "유효성 검사 실패",
                        fieldErrorList
                ));
    }

    /**
     * 필수 요청 쿠키 누락 예외 처리
     * @param ex 필수 쿠키 누락 시 발생하는 예외 처리
     * @return ErrorResponse를 포함한 ResponseEntity
     */
    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<ErrorResponse> handleMissingCookieException(MissingRequestCookieException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        HttpStatus.BAD_REQUEST,
                        "쿠키(리프레시 토큰) 누락"
                ));
    }
}

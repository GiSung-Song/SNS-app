package com.outsta.sns.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * API 성공 응답 DTO
 * @param <T> 응답 데이터 타입
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class SuccessResponse<T> {

    /** HTTP 상태 코드 */
    private final int status;

    /** 응답 메시지 */
    private final String message;

    /** 응답 데이터 */
    private final T data;

    /**
     * 상태코드와 데이터만 받는 생성자
     * - 기본 응답 메시지 설정
     *
     * @param status HTTP 상태 코드
     * @param data   응답 데이터
     */
    public SuccessResponse(int status, T data) {
        this.status = status;
        this.message = "성공";
        this.data = data;
    }

    /**
     * 상태코드와 응답 메시지, 응답 데이터를 받는 생성자
     *
     * @param status  HTTP 상태 코드
     * @param message 응답 메시지
     * @param data    응답 데이터
     */
    public SuccessResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    /**
     * HTTP 상태 코드, 응답 메시지, 응답 코드로 객체 생성 (상태 코드, 데이터, 메시지)
     */
    public static <T> SuccessResponse<T> of(HttpStatus status, String message, T data) {
        return new SuccessResponse<>(status.value(), message, data);
    }

    /**
     * HTTP 상태 코드, 응답 데이터로 객체 생성 (상태 코드, 데이터, 메시지 성공)
     */
    public static <T> SuccessResponse<T> of(HttpStatus status, T data) {
        return new SuccessResponse<>(status.value(), data);
    }

    /**
     * HTTP 상태 코드로 객체 생성 (상태 코드, 데이터 null, 메시지 성공)
     */
    public static <T> SuccessResponse<T> of(HttpStatus status) {
        return new SuccessResponse<>(status.value(),null);
    }

    /**
     * 응답 데이터로 객체 생성 (상태 코드 200 OK, 데이터 data, 메시지 성공)
     */
    public static <T> SuccessResponse<T> of(T data) {
        return new SuccessResponse<>(HttpStatus.OK.value(), data);
    }

    /**
     * 기본 성공 객체 생성 (상태 코드 200 OK, 데이터 null, 메시지 성공)
     */
    public static <T> SuccessResponse<T> of() {
        return new SuccessResponse<>(HttpStatus.OK.value(), null);
    }
}
package com.outsta.sns.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class SuccessResponse<T> {

    private final int status;
    private final String message;
    private final T data;

    public SuccessResponse(int status, T data) {
        this.status = status;
        this.message = "성공";
        this.data = data;
    }

    public SuccessResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> SuccessResponse<T> of(HttpStatus status, String message, T data) {
        return new SuccessResponse<>(status.value(), message, data);
    }

    public static <T> SuccessResponse<T> of(HttpStatus status, T data) {
        return new SuccessResponse<>(status.value(), data);
    }

    public static <T> SuccessResponse<T> of(HttpStatus status) {
        return new SuccessResponse<>(status.value(),null);
    }

    public static <T> SuccessResponse<T> of() {
        return new SuccessResponse<>(HttpStatus.OK.value(), null);
    }
}
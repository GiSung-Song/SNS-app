package com.outsta.sns.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "중복된 닉네임입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "중복된 이메일입니다."),
    DUPLICATE_MEMBER(HttpStatus.CONFLICT, "중복된 회원입니다."),

    SEND_MAIL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 전송 중 오류가 발생하였습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;
}

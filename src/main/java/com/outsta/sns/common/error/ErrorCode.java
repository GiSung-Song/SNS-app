package com.outsta.sns.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 애플리케이션에서 사용하는 공통 에러 코드 enum
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    /** 잘못된 요청 : 400 반환 */
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

    /** 잘못된 인증번호 : 401 반환 */
    INVALID_CODE(HttpStatus.UNAUTHORIZED, "이메일 혹은 인증번호가 틀렸습니다."),

    /** 정지된 회원 : 401 반환 */
    SUSPENDED_MEMBER(HttpStatus.UNAUTHORIZED, "활동 정지된 회원입니다."),

    /** 회원 탈퇴 대기중인 회원 : 401 반환 */
    WAITING_DELETED_MEMBER(HttpStatus.UNAUTHORIZED, "탈퇴 대기중인 회원입니다."),

    /** 만료된 토큰 : 401 반환 */
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),

    /** 유효하지 않는 토큰 : 401 반환 */
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),

    /** 유효하지 않은 이메일 혹은 비밀번호 : 401 반환 */
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 혹은 비밀번호를 확인해주세요."),

    /** 차단 상태 : 403 반환 */
    BLOCK_MEMBER(HttpStatus.FORBIDDEN, "차단 상태로 인해 접근이 제한됩니다."),

    /** 비공개 : 403 반환 */
    VISIBILITY_PRIVATE(HttpStatus.FORBIDDEN, "비공개입니다."),

    /** 팔로워 전용 : 403 반환 */
    VISIBILITY_FOLLOWER_ONLY(HttpStatus.FORBIDDEN, "팔로워 전용입니다."),

    /** 존재하지 않는 회원 : 404 반환 */
    NOT_FOUND_MEMBER(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),

    /** 존재하지 않는 이미지 : 404 반환 */
    NOT_FOUND_PROFILE_IMAGE(HttpStatus.NOT_FOUND, "존재하지 않는 프로필 이미지입니다."),

    /** 중복된 닉네임 : 409 반환 */
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "중복된 닉네임입니다."),

    /** 중복된 이메일 : 409 반환 */
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "중복된 이메일입니다."),

    /** 중복된 회원 : 409 반환 */
    DUPLICATE_MEMBER(HttpStatus.CONFLICT, "중복된 회원입니다."),

    /** 이미 차단한 회원 : 409 반환 */
    DUPLICATE_BLOCKED(HttpStatus.CONFLICT, "이미 차단한 회원입니다."),

    /** 이미 팔로우한 회원 : 409 반환 */
    DUPLICATE_FOLLOW(HttpStatus.CONFLICT, "이미 팔로우한 회원입니다."),

    /** 이미 인증된 회원 : 409 반환 */
    ALREADY_AUTHENTICATED_MEMBER(HttpStatus.CONFLICT, "이미 인증된 회원입니다."),

    /** 토큰 생성 중 내부 오류 : 500 반환 */
    MISSING_JWT_PAYLOAD(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 생성 중 오류가 발생하였습니다."),

    /** 이메일 전송 중 내부 오류 : 500 반환 */
    SEND_MAIL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 전송 중 오류가 발생하였습니다."),

    /** REDIS 정보 저장 중 내부 오류 : 500 반환 */
    REDIS_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "정보 저장 중 오류가 발생하였습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;
}

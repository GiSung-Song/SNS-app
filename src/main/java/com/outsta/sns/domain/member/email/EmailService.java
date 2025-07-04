package com.outsta.sns.domain.member.email;

import com.outsta.sns.common.error.CustomException;
import com.outsta.sns.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 이메일 인증 관련 서비스
 *
 * <p>인증 코드 및 임시 비밀번호 전송 기능 제공</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String CODE_PREFIX = "CODE:";

    /**
     * 비동기로 인증 코드 Redis에 저장 및 이메일로 전송
     * - 인증 코드를 5분간 저장
     * - 인증 코드를 해당 이메일로 전송
     *
     * @param email 이메일
     * @param code  인증 코드
     */
    @Async
    public void sendCode(String email, String code) {
        String subject = "OUTSTA 회원가입 인증 코드";
        String content = "인증 코드 : " + code;

        saveWithRetry(email, code, CODE_PREFIX);
        sendWithRetry(email, subject, content);
    }

    /**
     * 비동기로 임시 비밀번호 이메일로 전송
     * - 임시 비밀번호를 해당 이메일로 전송
     *
     * @param email        이메일
     * @param tempPassword 임시 비밀번호
     */
    @Async
    public void sendTempPassword(String email, String tempPassword) {
        String subject = "OUTSTA 임시 비밀번호";
        String content = "임시 비밀번호 : " + tempPassword;

        sendWithRetry(email, subject, content);
    }

    /**
     * 이메일 전송
     * - EmailException 발생 시 재시도
     *
     * @param email   이메일
     * @param subject 제목
     * @param content 내용
     */
    @Retryable(
            retryFor = { MailException.class },
            backoff = @Backoff(delay = 2000)
    )
    public void sendWithRetry(String email, String subject, String content) {
        sendSimpleMailMessage(email, subject, content);
    }

    /**
     * Redis 저장
     * - RedisConnectionFailureException 발생 시 재시도
     * @param email  이메일
     * @param value  값
     * @param prefix 접두사
     */
    @Retryable(
            retryFor = { RedisConnectionFailureException.class },
            backoff = @Backoff(delay = 2000)
    )
    public void saveWithRetry(String email, String value, String prefix) {
        redisTemplate.opsForValue().set(prefix + email, value, Duration.ofMinutes(5));
    }

    /**
     * Redis 저장 재시도 실패 시 로그 출력 및 CustomException 발생
     *
     * @param e      오류
     * @param email  이메일
     * @param value  값
     * @param prefix 접두사
     */
    @Recover
    public void recover(RedisConnectionFailureException e, String email, String value, String prefix) {
        log.error("[재시도 실패 - Redis 저장 오류] email: {}, value: {}, prefix: {}, error: {}", email, value, prefix, e.getMessage());
        throw new CustomException(ErrorCode.REDIS_INTERNAL_ERROR);
    }

    /**
     * 이메일 전송 재시도 실패 시 로그 출력 및 CustomException 발생
     *
     * @param e       오류
     * @param email   이메일
     * @param subject 제목
     * @param content 내용
     */
    @Recover
    public void recover(MailException e, String email, String subject, String content) {
        log.error("[재시도 실패 - 이메일 전송 오류] email: {}, subject: {}, content: {}, error: {}", email, subject, content, e.getMessage());
        throw new CustomException(ErrorCode.SEND_MAIL_ERROR);
    }

    /**
     * 이메일 전송
     * - Gmail 이용중
     * @param email   이메일
     * @param subject 제목
     * @param content 내용
     */
    private void sendSimpleMailMessage(String email, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject(subject);
        message.setText(content);

        mailSender.send(message);
    }
}

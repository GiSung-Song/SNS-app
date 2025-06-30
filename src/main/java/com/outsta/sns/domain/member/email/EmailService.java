package com.outsta.sns.domain.member.email;

import com.outsta.sns.common.error.CustomException;
import com.outsta.sns.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private static final String CHAR_POOL = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int VALUE_LENGTH = 10;
    private final SecureRandom random = new SecureRandom();

    public String generateRandomValue() {
        StringBuilder sb = new StringBuilder(VALUE_LENGTH);

        for (int i = 0; i < VALUE_LENGTH; i++) {
            int idx = random.nextInt(CHAR_POOL.length());

            sb.append(CHAR_POOL.charAt(idx));
        }

        return sb.toString();
    }

    private void sendSimpleMailMessage(String email, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(email);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);
        } catch (MailException e) {
            throw new CustomException(ErrorCode.SEND_MAIL_ERROR);
        }
    }

    public void sendCode(String email) {
        String code = generateRandomValue();

        String subject = "OUTSTA 회원가입 인증 코드";
        String content = "인증 코드 : " + code;

        sendSimpleMailMessage(email, subject, content);
    }

    public void sendTempPassword(String email) {
        String tempPassword = generateRandomValue();

        String subject = "OUTSTA 임시 비밀번호";
        String content = "임시 비밀번호 : " + tempPassword;

        sendSimpleMailMessage(email, subject, content);
    }
}

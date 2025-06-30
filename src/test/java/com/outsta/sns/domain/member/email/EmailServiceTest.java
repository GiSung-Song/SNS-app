package com.outsta.sns.domain.member.email;

import com.outsta.sns.common.error.CustomException;
import com.outsta.sns.common.error.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void 인증번호_전송_성공() {
        doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));

        emailService.sendCode("test@test.com");

        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void 임시_비밀번호_전송_성공() {
        doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));

        emailService.sendTempPassword("test@test.com");

        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void 메일_전송_실패할_떄_500_반환() {
        doThrow(new MailException("이메일 오류") {}).when(javaMailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> emailService.sendCode("test@test.com"))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException exception = (CustomException) ex;
                    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SEND_MAIL_ERROR);
                });
    }

}
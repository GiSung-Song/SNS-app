package com.outsta.sns.domain.member.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void 인증번호_전송_성공() {
        doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));

        emailService.sendCode("test@test.com", "1234123412");

        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void 임시_비밀번호_전송_성공() {
        doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));

        emailService.sendTempPassword("test@test.com", "1234123412");

        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

}
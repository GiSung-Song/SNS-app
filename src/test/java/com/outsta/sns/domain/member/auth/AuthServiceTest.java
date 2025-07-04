package com.outsta.sns.domain.member.auth;

import com.outsta.sns.common.config.security.JwtPayload;
import com.outsta.sns.common.config.security.JwtProvider;
import com.outsta.sns.common.error.CustomException;
import com.outsta.sns.common.error.ErrorCode;
import com.outsta.sns.config.DBContainerExtension;
import com.outsta.sns.config.TestDataFactory;
import com.outsta.sns.config.support.ServiceTestSupport;
import com.outsta.sns.domain.member.auth.dto.LoginRequest;
import com.outsta.sns.domain.member.auth.dto.TokenDto;
import com.outsta.sns.domain.member.entity.Member;
import com.outsta.sns.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthServiceTest extends ServiceTestSupport {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProvider jwtProvider;

    @Nested
    class 로그인_테스트 {

        @Test
        void 로그인_성공_시_토큰_DTO_반환() {
            Member tester = testDataFactory.createTester();

            LoginRequest request = new LoginRequest(tester.getEmail(), "password");

            TokenDto tokenDto = authService.login(request);

            assertThat(tokenDto.accessToken()).isNotNull();
            assertThat(tokenDto.refreshToken()).isNotNull();

            String storedRefreshToken = redisTemplate.opsForValue().get("refresh:" + tester.getId());

            assertThat(storedRefreshToken).isEqualTo(tokenDto.refreshToken());
        }

        @Test
        void 비밀번호_틀릴_시_401_반환() {
            Member tester = testDataFactory.createTester();

            LoginRequest request = new LoginRequest(tester.getEmail(), "password1234");

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    });
        }
    }

    @Nested
    class 로그아웃_테스트 {

        @Test
        void 로그아웃_성공() {
            Member tester = testDataFactory.createTester();

            JwtPayload jwtPayload = new JwtPayload(tester.getId(), tester.getEmail(), tester.getRole().getCode());

            String accessToken = jwtProvider.generateAccessToken(jwtPayload);
            String hashToken = jwtProvider.tokenToHash(accessToken);

            authService.logout(accessToken);

            String result = redisTemplate.opsForValue().get(hashToken);
            assertThat(result).isEqualTo("logout");
        }
    }

    @Nested
    class 토큰_재발급_테스트 {

        @Test
        void 토큰_재발급_성공_시_토큰_DTO_반환() {
            Member tester = testDataFactory.createTester();

            JwtPayload jwtPayload = new JwtPayload(tester.getId(), tester.getEmail(), tester.getRole().getCode());

            String refreshToken = jwtProvider.generateRefreshToken(jwtPayload);

            redisTemplate.opsForValue().set("refresh:" + tester.getId(), refreshToken, jwtProvider.getRefreshTokenExpiration(), TimeUnit.MILLISECONDS);

            TokenDto tokenDto = authService.reIssueToken(refreshToken);

            assertThat(tokenDto.accessToken()).isNotNull();
        }

        @Test
        void 유효하지_않은_토큰일_시_401_반환() {
            assertThatThrownBy(() -> authService.reIssueToken("invalid.refresh.token"))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    });
        }
    }
}
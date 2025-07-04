package com.outsta.sns.common.config.security;

import com.outsta.sns.common.error.CustomException;
import com.outsta.sns.common.error.ErrorCode;
import com.outsta.sns.domain.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        String secretKey = "sdapifjpi324jpifqhidpashf803h280i1fhidshaf80h340281q";
        Long accessExpiration = 1000 * 60 * 1L;
        Long refreshExpiration = 1000 * 60 * 60L;

        jwtProvider = new JwtProvider(secretKey, accessExpiration, refreshExpiration);
    }

    @Test
    void 액세스_토큰_생성_및_파싱_성공() {
        JwtPayload jwtPayload = new JwtPayload();

        jwtPayload.setId(1L);
        jwtPayload.setEmail("test@test.com");
        jwtPayload.setRole(Role.GUEST.getCode());

        String accessToken = jwtProvider.generateAccessToken(jwtPayload);
        JwtPayload parsedToken = jwtProvider.parseAccessToken(accessToken);

        assertThat(parsedToken.getId()).isEqualTo(jwtPayload.getId());
        assertThat(parsedToken.getEmail()).isEqualTo(jwtPayload.getEmail());
        assertThat(parsedToken.getRole()).isEqualTo(jwtPayload.getRole());
    }

    @Test
    void 리프레시_토큰_생성_및_파싱_성공() {
        JwtPayload jwtPayload = new JwtPayload();

        jwtPayload.setId(1L);

        String refreshToken = jwtProvider.generateRefreshToken(jwtPayload);
        Long memberId = jwtProvider.parseRefreshToken(refreshToken);

        assertThat(memberId).isEqualTo(jwtPayload.getId());
    }

    @Test
    void 만료된_토큰_파싱할_시_401_반환() throws InterruptedException {
        JwtPayload jwtPayload = new JwtPayload();

        jwtPayload.setId(1L);
        jwtPayload.setEmail("test@test.com");
        jwtPayload.setRole(Role.GUEST.getCode());

        JwtProvider shortJwtProvider = new JwtProvider(
                "fdsaoifjdoaifjajrui3ej2091j390fdj09wafj0pdas", 1L, 1L);

        String shortAccessToken = shortJwtProvider.generateAccessToken(jwtPayload);

        Thread.sleep(10);

        assertThatThrownBy(() -> shortJwtProvider.parseAccessToken(shortAccessToken))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException exception = (CustomException) ex;

                    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EXPIRED_TOKEN);
                    assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                });
    }

    @Test
    void 유효하지_않은_토큰_파싱할_시_401_반환() {
        String invalidToken = "jwt.invalid.token";

        assertThatThrownBy(() -> jwtProvider.parseAccessToken(invalidToken))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException exception = (CustomException) ex;

                    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN);
                    assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                });
    }

    @Test
    void 만료된_토큰_여부_확인할_시_만료되었으면_true_반환() throws InterruptedException {
        JwtPayload jwtPayload = new JwtPayload();

        jwtPayload.setId(1L);
        jwtPayload.setEmail("test@test.com");
        jwtPayload.setRole(Role.GUEST.getCode());

        JwtProvider shortJwtProvider = new JwtProvider(
                "fdsaoifjdoaifjajrui3ej2091j390fdj09wafj0pdas", 1L, 1L);

        String shortAccessToken = shortJwtProvider.generateAccessToken(jwtPayload);

        Thread.sleep(10);

        assertThat(jwtProvider.isTokenExpired(shortAccessToken)).isTrue();
    }

    @Test
    void 토큰_검사할_시_정상_토큰이면_true_반환() {
        JwtPayload jwtPayload = new JwtPayload();

        jwtPayload.setId(1L);
        jwtPayload.setEmail("test@test.com");
        jwtPayload.setRole(Role.GUEST.getCode());

        String accessToken = jwtProvider.generateAccessToken(jwtPayload);

        assertThat(jwtProvider.validateToken(accessToken)).isTrue();
    }

    @Test
    void 토큰_해쉬_정상() {
        JwtPayload jwtPayload = new JwtPayload();

        jwtPayload.setId(1L);
        jwtPayload.setEmail("test@test.com");
        jwtPayload.setRole(Role.GUEST.getCode());

        String accessToken = jwtProvider.generateAccessToken(jwtPayload);

        String hashToken = jwtProvider.tokenToHash(accessToken);

        assertThat(hashToken).hasSize(64);
    }
}
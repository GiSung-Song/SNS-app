package com.outsta.sns.domain.member.auth;

import com.outsta.sns.common.config.security.JwtPayload;
import com.outsta.sns.common.config.security.JwtProvider;
import com.outsta.sns.config.support.ControllerTestSupport;
import com.outsta.sns.domain.enums.Role;
import com.outsta.sns.domain.member.auth.dto.LoginRequest;
import com.outsta.sns.domain.member.entity.Member;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest extends ControllerTestSupport {

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Nested
    class 로그인_테스트 {

        @Test
        void 로그인_정상_시_토큰_DTO_반환() throws Exception {
            Member member = testDataFactory.createTester();

            LoginRequest request = new LoginRequest(member.getEmail(), "password");

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(result -> {
                        String refreshToken = result.getResponse().getCookie("refreshToken").getValue();

                        assertThat(refreshToken).isNotNull();
                        assertThat(refreshToken).isNotEmpty();
                    });
        }

        @Test
        void 아이디_혹은_비밀번호_누락_시_400_반환() throws Exception {
            LoginRequest request = new LoginRequest(null, "password1313");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        void 비밀번호_틀릴_시_401_반환() throws Exception {
            Member member = testDataFactory.createTester();

            LoginRequest request = new LoginRequest(member.getEmail(), "password1313");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }

    @Nested
    class 로그아웃_테스트 {

        @Test
        @WithMockUser(roles = "MEMBER")
        void 로그아웃_성공() throws Exception {
            Member tester = testDataFactory.createTester();

            JwtPayload jwtPayload = new JwtPayload(tester.getId(), tester.getEmail(), tester.getRole().getCode());

            String accessToken = jwtProvider.generateAccessToken(jwtPayload);

            mockMvc.perform(post("/api/auth/logout")
                    .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().string("Set-Cookie", allOf(
                            containsString("refreshToken=deleted"),
                            containsString("Max-Age=0"),
                            containsString("Path=/api/auth/reissue"),
                            containsString("HttpOnly"),
                            containsString("Secure"),
                            containsString("SameSite=None")
                    )));

            String hashToken = jwtProvider.tokenToHash(accessToken);
            String logout = redisTemplate.opsForValue().get(hashToken);
            assertThat(logout).isEqualTo("logout");
        }

        @Test
        void 토큰_없을_시_401_반환() throws Exception {
            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }

    @Nested
    class 토큰_재발급_테스트 {

        @Test
        void 토큰_재발급_성공() throws Exception {
            Member tester = testDataFactory.createTester();
            JwtPayload jwtPayload = new JwtPayload(tester.getId(), tester.getEmail(), tester.getRole().getCode());

            String refreshToken = jwtProvider.generateRefreshToken(jwtPayload);
            redisTemplate.opsForValue().set("refresh:" + tester.getId(), refreshToken);

            mockMvc.perform(post("/api/auth/reissue")
                            .cookie(new Cookie("refreshToken", refreshToken)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
        }

        @Test
        void 쿠키_누락_시_400_반환() throws Exception {
            mockMvc.perform(post("/api/auth/reissue"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 토큰_유효하지_않을_시_401_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            JwtPayload jwtPayload = new JwtPayload(tester.getId(), tester.getEmail(), tester.getRole().getCode());

            String refreshToken = jwtProvider.generateRefreshToken(jwtPayload);
            redisTemplate.opsForValue().set("refresh:" + tester.getId(), refreshToken);

            mockMvc.perform(post("/api/auth/reissue")
                            .cookie(new Cookie("refreshToken", "invalid.jwt.token")))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void 존재하지_않는_회원일_시_404_반환() throws Exception {
            JwtPayload jwtPayload = new JwtPayload(43213421L, "1234@1234.com", Role.MEMBER.getCode());

            String refreshToken = jwtProvider.generateRefreshToken(jwtPayload);
            redisTemplate.opsForValue().set("refresh:" + jwtPayload.getId(), refreshToken);

            mockMvc.perform(post("/api/auth/reissue")
                            .cookie(new Cookie("refreshToken", refreshToken)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }
}
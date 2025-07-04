package com.outsta.sns.domain.member.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.outsta.sns.config.support.ControllerTestSupport;
import com.outsta.sns.domain.enums.Activation;
import com.outsta.sns.domain.enums.Role;
import com.outsta.sns.domain.enums.Visibility;
import com.outsta.sns.domain.member.dto.request.*;
import com.outsta.sns.domain.member.email.EmailService;
import com.outsta.sns.domain.member.entity.Member;
import com.outsta.sns.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MemberControllerTest extends ControllerTestSupport {

    @MockBean
    private EmailService emailService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Nested
    class 회원가입_API_테스트 {

        @Test
        void 정상_회원가입() throws Exception {
            SignUpRequest request = new SignUpRequest(
                    "데이비드",
                    "password",
                    "david",
                    "david@junior.com",
                    LocalDate.of(1995, 12, 12),
                    "MALE"
            );

            mockMvc.perform(post("/api/members")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andDo(print());

            verify(emailService, times(1)).sendCode(eq(request.email()), anyString());

            Member findMember = memberRepository.findByEmail(request.email())
                    .orElseThrow();

            assertThat(findMember.getNickname()).isEqualTo(request.nickname());
            assertThat(findMember.getBirth()).isEqualTo(request.birth());
        }

        @Test
        void 필수값_누락_시_400_반환() throws Exception {
            SignUpRequest request = new SignUpRequest(
                    "데이비드",
                    null,
                    "david",
                    "david@junior.com",
                    LocalDate.of(1995, 12, 12),
                    "MALE"
            );

            mockMvc.perform(post("/api/members")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        void 중복된_이메일_혹은_닉네임일_시_409_반환() throws Exception {
            Member tester = testDataFactory.createTester();

            SignUpRequest request = new SignUpRequest(
                    "데이비드",
                    "password",
                    tester.getNickname(),
                    "david@junior.com",
                    LocalDate.of(1995, 12, 12),
                    "MALE"
            );

            mockMvc.perform(post("/api/members")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andDo(print());
        }
    }

    @Nested
    class 닉네임_중복_체크_API_테스트 {

        @Test
        void 닉네임_중복_체크_정상_시_boolean_반환() throws Exception {
            Member dancer = testDataFactory.createDancer();

            NicknameCheckRequest nicknameCheckRequest = new NicknameCheckRequest(dancer.getNickname());

            mockMvc.perform(get("/api/members/check-nickname")
                            .param("nickname", nicknameCheckRequest.nickname()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(jsonPath("$.data.duplicated").value(true));
        }

        @Test
        void 파라미터_없을_시_400_반환() throws Exception {
            mockMvc.perform(get("/api/members/check-nickname"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    class 이메일_중복_체크_API_테스트 {

        @Test
        void 이메일_중복_체크_정상_시_boolean_반환() throws Exception {
            Member dancer = testDataFactory.createDancer();

            EmailCheckRequest request = new EmailCheckRequest(dancer.getEmail());

            mockMvc.perform(get("/api/members/check-email")
                            .param("email", request.email()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(jsonPath("$.data.duplicated").value(true));
        }

        @Test
        void 파라미터_없을_시_400_반환() throws Exception {
            mockMvc.perform(get("/api/members/check-email"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    class 닉네임_수정_API_테스트 {

        @Test
        void 닉네임_정상_수정_시_회원_ID_반환() throws Exception {
            Member dancer = testDataFactory.createDancer();
            testDataFactory.setAuthentication(dancer);

            NicknameUpdateRequest request = new NicknameUpdateRequest("ddancer");

            mockMvc.perform(patch("/api/members/me/nickname")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andDo(print());

            Member findDancer = memberRepository.findById(dancer.getId())
                    .orElseThrow();

            assertThat(findDancer.getNickname()).isEqualTo(request.nickname());
        }

        @Test
        void 필수값_누락_시_400_반환() throws Exception {
            Member dancer = testDataFactory.createDancer();
            testDataFactory.setAuthentication(dancer);

            NicknameUpdateRequest request = new NicknameUpdateRequest(null);

            mockMvc.perform(patch("/api/members/me/nickname")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        void 비로그인_시_401_반환() throws Exception {
            NicknameUpdateRequest request = new NicknameUpdateRequest("ddancer");

            mockMvc.perform(patch("/api/members/me/nickname")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        void 닉네임_중복_시_409_반환() throws Exception {
            Member dancer = testDataFactory.createDancer();
            Member faker = testDataFactory.createFaker();

            testDataFactory.setAuthentication(dancer);

            NicknameUpdateRequest request = new NicknameUpdateRequest(faker.getNickname());

            mockMvc.perform(patch("/api/members/me/nickname")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andDo(print());
        }
    }

    @Nested
    class 비밀번호_수정_API_테스트 {

        @Test
        void 비밀번호_정상_수정_시_회원_ID_반환() throws Exception {
            Member dancer = testDataFactory.createDancer();
            testDataFactory.setAuthentication(dancer);

            PasswordUpdateRequest request = new PasswordUpdateRequest("editPassword");

            mockMvc.perform(patch("/api/members/me/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andDo(print());

            Member findDancer = memberRepository.findById(dancer.getId())
                    .orElseThrow();

            assertThat(passwordEncoder.matches(request.password(), findDancer.getPassword())).isTrue();
        }

        @Test
        void 필수값_누락_시_400_반환() throws Exception {
            Member dancer = testDataFactory.createDancer();
            testDataFactory.setAuthentication(dancer);

            PasswordUpdateRequest request = new PasswordUpdateRequest(null);

            mockMvc.perform(patch("/api/members/me/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        void 비로그인_시_401_반환() throws Exception {
            PasswordUpdateRequest request = new PasswordUpdateRequest("newPassword");

            mockMvc.perform(patch("/api/members/me/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }

    @Nested
    class 회원_탈퇴_API_테스트 {

        @Test
        void 정상_탈퇴_시_상태_변경() throws Exception {
            Member dancer = testDataFactory.createDancer();
            testDataFactory.setAuthentication(dancer);

            mockMvc.perform(delete("/api/members/me"))
                    .andDo(print())
                    .andExpect(status().isOk());

            Member findMember = memberRepository.findById(dancer.getId())
                    .orElseThrow();

            assertThat(findMember.getActivation()).isEqualTo(Activation.WAITING_DELETED);
            assertThat(findMember.getDeletedAt()).isNotNull();
        }

        @Test
        void 비로그인_시_401_반환() throws Exception {
            mockMvc.perform(delete("/api/members/members/me")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }

    @Nested
    class 프로필_정보_공개_범위_설정_API_테스트 {

        @Test
        void 정상_설정_시_회원_ID_반환() throws Exception {
            Member dancer = testDataFactory.createDancer();
            testDataFactory.setAuthentication(dancer);

            PrivacyUpdateRequest request = new PrivacyUpdateRequest("FOLLOWER_ONLY");

            MvcResult result = mockMvc.perform(patch("/api/members/me/privacy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode dataNode = root.get("data");
            long memberId = dataNode.get("memberId").asLong();

            Member findMember = memberRepository.findById(memberId)
                    .orElseThrow();

            assertThat(findMember.getVisibility()).isEqualTo(Visibility.FOLLOWER_ONLY);
        }

        @Test
        void 필수값_누락_시_400_반환() throws Exception {
            Member dancer = testDataFactory.createDancer();
            testDataFactory.setAuthentication(dancer);

            PrivacyUpdateRequest request = new PrivacyUpdateRequest(null);

            mockMvc.perform(patch("/api/members/me/privacy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        void 비로그인_시_401_반환() throws Exception {
            PrivacyUpdateRequest request = new PrivacyUpdateRequest("FOLLOWER_ONLY");

            mockMvc.perform(patch("/api/members/me/privacy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }

    @Nested
    class 비밀번호_초기화_API_테스트 {

        @Test
        void 비밀번호_초기화_정상() throws Exception {
            Member dancer = testDataFactory.createDancer();
            testDataFactory.setAuthentication(dancer);

            PasswordResetRequest request = new PasswordResetRequest(
                    dancer.getName(),
                    dancer.getEmail(),
                    dancer.getBirth()
            );

            mockMvc.perform(post("/api/members/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andDo(print());

            verify(emailService, times(1)).sendTempPassword(eq(dancer.getEmail()), anyString());

            Member findMember = memberRepository.findById(dancer.getId())
                    .orElseThrow();

            assertThat(passwordEncoder.matches("password", findMember.getPassword())).isFalse();
        }

        @Test
        void 필수값_누락_시_400_반환() throws Exception {
            Member dancer = testDataFactory.createDancer();
            testDataFactory.setAuthentication(dancer);

            PasswordResetRequest request = new PasswordResetRequest(
                    dancer.getName(),
                    dancer.getEmail(),
                    null
            );

            mockMvc.perform(post("/api/members/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        void 이메일_생년월일_이름_하나라도_틀릴_시_404_반환() throws Exception {
            Member dancer = testDataFactory.createDancer();
            testDataFactory.setAuthentication(dancer);

            PasswordResetRequest request = new PasswordResetRequest(
                    dancer.getName(),
                    dancer.getEmail(),
                    dancer.getBirth().plusDays(1)
            );

            mockMvc.perform(post("/api/members/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }
    }

    @Nested
    class 회원_탈퇴_취소_API_테스트 {

        @Test
        void 회원_탈퇴_정상_취소() throws Exception {
            Member dancer = testDataFactory.createDancer();
            dancer.deleteMember();

            Member deletedMember = memberRepository.findById(dancer.getId())
                    .orElseThrow();

            assertThat(deletedMember.getDeletedAt()).isNotNull();
            assertThat(deletedMember.getActivation()).isEqualTo(Activation.WAITING_DELETED);

            CancelDeleteRequest request = new CancelDeleteRequest(
                    dancer.getName(),
                    "password",
                    dancer.getEmail(),
                    dancer.getBirth()
            );

            mockMvc.perform(patch("/api/members/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk());

            Member findMember = memberRepository.findById(dancer.getId())
                    .orElseThrow();

            assertThat(findMember.getDeletedAt()).isNull();
            assertThat(findMember.getActivation()).isEqualTo(Activation.ACTIVE);
        }

        @Test
        void 필수값_누락_시_400_반환() throws Exception {
            Member dancer = testDataFactory.createDancer();
            dancer.deleteMember();

            Member deletedMember = memberRepository.findById(dancer.getId())
                    .orElseThrow();

            assertThat(deletedMember.getDeletedAt()).isNotNull();
            assertThat(deletedMember.getActivation()).isEqualTo(Activation.WAITING_DELETED);

            CancelDeleteRequest request = new CancelDeleteRequest(
                    dancer.getName(),
                    "password",
                    dancer.getEmail(),
                    null
            );

            mockMvc.perform(patch("/api/members/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 탈퇴_처리_되지_않은_회원일_시_400_반환() throws Exception {
            Member dancer = testDataFactory.createDancer();

            CancelDeleteRequest request = new CancelDeleteRequest(
                    dancer.getName(),
                    "password",
                    dancer.getEmail(),
                    dancer.getBirth()
            );

            mockMvc.perform(patch("/api/members/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 비밀번호만_틀릴_시_401_반환() throws Exception {
            Member dancer = testDataFactory.createDancer();
            dancer.deleteMember();

            Member deletedMember = memberRepository.findById(dancer.getId())
                    .orElseThrow();

            assertThat(deletedMember.getDeletedAt()).isNotNull();
            assertThat(deletedMember.getActivation()).isEqualTo(Activation.WAITING_DELETED);

            CancelDeleteRequest request = new CancelDeleteRequest(
                    dancer.getName(),
                    "password1",
                    dancer.getEmail(),
                    dancer.getBirth()
            );

            mockMvc.perform(patch("/api/members/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void 이름_혹은_이메일_혹은_생년월일_중_하나라도_틀릴_시_404_반환() throws Exception {
            Member dancer = testDataFactory.createDancer();
            dancer.deleteMember();

            Member deletedMember = memberRepository.findById(dancer.getId())
                    .orElseThrow();

            assertThat(deletedMember.getDeletedAt()).isNotNull();
            assertThat(deletedMember.getActivation()).isEqualTo(Activation.WAITING_DELETED);

            CancelDeleteRequest request = new CancelDeleteRequest(
                    dancer.getName(),
                    "password",
                    dancer.getEmail(),
                    dancer.getBirth().plusDays(1)
            );

            mockMvc.perform(patch("/api/members/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class 인증_코드_확인_API_테스트 {

        private static final String REDIS_PREFIX = "CODE:";
        private static final String REDIS_VALUE = "a1A2b1B2cd";

        @Test
        void 정상_인증_시_회원_Role_변경() throws Exception {
            Member guest = testDataFactory.createGuest();

            redisTemplate.opsForValue().set(REDIS_PREFIX + guest.getEmail(), REDIS_VALUE, Duration.ofMinutes(5));

            CodeCheckRequest request = new CodeCheckRequest(
                    guest.getEmail(),
                    REDIS_VALUE
            );

            mockMvc.perform(post("/api/members/code-verification")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk());

            Member findMember = memberRepository.findById(guest.getId())
                    .orElseThrow();

            assertThat(findMember.getRole()).isEqualTo(Role.MEMBER);
        }

        @Test
        void 필수값_누락_시_400_반환() throws Exception {
            Member guest = testDataFactory.createGuest();

            redisTemplate.opsForValue().set(REDIS_PREFIX + guest.getEmail(), REDIS_VALUE, Duration.ofMinutes(5));

            CodeCheckRequest request = new CodeCheckRequest(
                    guest.getEmail(),
                    null
            );

            mockMvc.perform(post("/api/members/code-verification")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 인증번호_틀릴_시_401_반환() throws Exception {
            Member guest = testDataFactory.createGuest();

            redisTemplate.opsForValue().set(REDIS_PREFIX + guest.getEmail(), REDIS_VALUE, Duration.ofMinutes(5));

            CodeCheckRequest request = new CodeCheckRequest(
                    guest.getEmail(),
                    "a1a1a1a1a1"
            );

            mockMvc.perform(post("/api/members/code-verification")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void 이미_인증된_회원일_시_409_반환() throws Exception {
            Member dancer = testDataFactory.createDancer();

            redisTemplate.opsForValue().set(REDIS_PREFIX + dancer.getEmail(), REDIS_VALUE, Duration.ofMinutes(5));

            CodeCheckRequest request = new CodeCheckRequest(
                    dancer.getEmail(),
                    REDIS_VALUE
            );

            mockMvc.perform(post("/api/members/code-verification")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    class 인증_코드_재발송_API_테스트 {

        @Test
        void 인증_코드_정상_재발송() throws Exception {
            Member guest = testDataFactory.createGuest();

            CodeReSendRequest request = new CodeReSendRequest(guest.getEmail());

            mockMvc.perform(post("/api/members/code-resend")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(emailService, times(1)).sendCode(eq(guest.getEmail()), anyString());
        }

        @Test
        void 필수값_누락_시_400반환() throws Exception {
            CodeReSendRequest request = new CodeReSendRequest(null);

            mockMvc.perform(post("/api/members/code-resend")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(emailService, never()).sendCode(anyString(), anyString());
        }

        @Test
        void Role이_Guest가_아니면_아무일도_없음() throws Exception {
            Member faker = testDataFactory.createFaker();

            CodeReSendRequest request = new CodeReSendRequest(faker.getEmail());

            mockMvc.perform(post("/api/members/code-resend")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(emailService, never()).sendCode(anyString(), anyString());
        }
    }

    private Member findByEmail(String email) {
        return memberRepository.findAll().stream()
                .filter(m -> m.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow();
    }

}
package com.outsta.sns.domain.member.service;

import com.outsta.sns.common.error.CustomException;
import com.outsta.sns.common.error.ErrorCode;
import com.outsta.sns.config.DBContainerExtension;
import com.outsta.sns.config.TestDataFactory;
import com.outsta.sns.domain.member.dto.repository.MemberRepository;
import com.outsta.sns.domain.member.dto.request.CheckEmailRequest;
import com.outsta.sns.domain.member.dto.request.CheckNicknameRequest;
import com.outsta.sns.domain.member.dto.request.SignUpRequest;
import com.outsta.sns.domain.member.dto.response.CheckEmailResponse;
import com.outsta.sns.domain.member.dto.response.CheckNicknameResponse;
import com.outsta.sns.domain.member.dto.response.MemberIdResponse;
import com.outsta.sns.domain.member.entity.Member;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(DBContainerExtension.class)
@Transactional
@SpringBootTest
@ActiveProfiles("test")
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestDataFactory testDataFactory;

    @Nested
    class 이메일_중복_테스트 {

        @Test
        void 중복된_이메일일_때_true_반환() {
            Member member = testDataFactory.createMember();

            CheckEmailRequest request = new CheckEmailRequest(member.getEmail());
            CheckEmailResponse response = memberService.checkDuplicateEmail(request);

            assertThat(response.isDuplicated()).isTrue();
        }

        @Test
        void 중복된_이메일이_아닐_때_false_반환() {
            CheckEmailRequest request = new CheckEmailRequest("tester1@email.com");
            CheckEmailResponse response = memberService.checkDuplicateEmail(request);

            assertThat(response.isDuplicated()).isFalse();
        }
    }

    @Nested
    class 닉네임_중복_테스트 {

        @Test
        void 중복된_닉네임일_때_true_반환() {
            Member member = testDataFactory.createMember();

            CheckNicknameRequest request = new CheckNicknameRequest(member.getNickname());
            CheckNicknameResponse response = memberService.checkDuplicateNickname(request);

            assertThat(response.isDuplicated()).isTrue();
        }

        @Test
        void 중복된_닉네임이_아닐_때_false_반환() {
            CheckNicknameRequest request = new CheckNicknameRequest("테스터1");
            CheckNicknameResponse response = memberService.checkDuplicateNickname(request);

            assertThat(response.isDuplicated()).isFalse();
        }
    }

    @Nested
    class 회원가입_테스트 {

        @Test
        void 회원가입_정상일_떄_id_반환() {
            SignUpRequest request = new SignUpRequest();

            request.setName("인디아나 존스");
            request.setBirth(LocalDate.of(1960, 7, 5));
            request.setEmail("indiana@johns.com");
            request.setGender("MALE");
            request.setPassword("rawPassword");
            request.setNickname("johns");

            MemberIdResponse response = memberService.signUpMember(request);
            Member member = memberRepository.findById(response.memberId()).orElseThrow();

            assertThat(member.getNickname()).isEqualTo(request.getNickname());
            assertThat(member.getBirth()).isEqualTo(request.getBirth());
            assertThat(member.getEmail()).isEqualTo(request.getEmail());
        }

        @Test
        void 중복된_이메일일_떄_409_반환() {
            Member member = testDataFactory.createMember();

            SignUpRequest request = new SignUpRequest();

            request.setName("인디아나 존스");
            request.setBirth(LocalDate.of(1960, 7, 5));
            request.setEmail(member.getEmail());
            request.setGender("MALE");
            request.setPassword("rawPassword");
            request.setNickname("johns");

            assertThatThrownBy(() -> memberService.signUpMember(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_EMAIL);
                        assertThat(ce.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
                    });
        }

        @Test
        void 중복된_닉네임일_떄_409_반환() {
            Member member = testDataFactory.createMember();

            SignUpRequest request = new SignUpRequest();

            request.setName("인디아나 존스");
            request.setBirth(LocalDate.of(1960, 7, 5));
            request.setEmail("indiana@johns.com");
            request.setGender("MALE");
            request.setPassword("rawPassword");
            request.setNickname(member.getNickname());

            assertThatThrownBy(() -> memberService.signUpMember(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_NICKNAME);
                        assertThat(ce.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
                    });
        }
    }
}
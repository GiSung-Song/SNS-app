package com.outsta.sns.domain.member.service;

import com.outsta.sns.common.error.CustomException;
import com.outsta.sns.common.error.ErrorCode;
import com.outsta.sns.config.support.ServiceTestSupport;
import com.outsta.sns.domain.enums.Activation;
import com.outsta.sns.domain.enums.Role;
import com.outsta.sns.domain.enums.Visibility;
import com.outsta.sns.domain.member.dto.request.*;
import com.outsta.sns.domain.member.dto.response.CheckEmailResponse;
import com.outsta.sns.domain.member.dto.response.CheckNicknameResponse;
import com.outsta.sns.domain.member.dto.response.MemberIdResponse;
import com.outsta.sns.domain.member.email.EmailService;
import com.outsta.sns.domain.member.entity.Member;
import com.outsta.sns.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class MemberServiceTest extends ServiceTestSupport {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private EmailService emailService;

    @Nested
    class 이메일_중복_테스트 {

        @Test
        void 중복된_이메일일_때_true_반환() {
            Member member = testDataFactory.createTester();

            CheckEmailResponse response = memberService.checkDuplicateEmail(member.getEmail());

            assertThat(response.duplicated()).isTrue();
        }

        @Test
        void 중복된_이메일이_아닐_때_false_반환() {
            CheckEmailResponse response = memberService.checkDuplicateEmail("tester1@email.com");

            assertThat(response.duplicated()).isFalse();
        }
    }

    @Nested
    class 닉네임_중복_테스트 {

        @Test
        void 중복된_닉네임일_때_true_반환() {
            Member member = testDataFactory.createTester();

            CheckNicknameResponse response = memberService.checkDuplicateNickname(member.getNickname());

            assertThat(response.duplicated()).isTrue();
        }

        @Test
        void 중복된_닉네임이_아닐_때_false_반환() {
            CheckNicknameResponse response = memberService.checkDuplicateNickname("테스터1");

            assertThat(response.duplicated()).isFalse();
        }
    }

    @Nested
    class 회원가입_테스트 {

        @Test
        void 회원가입_정상일_떄_id_반환() {
            SignUpRequest request = new SignUpRequest(
                    "인디아나 존스",
                    "rawPassword",
                    "johns",
                    "indiana@johns.com",
                    LocalDate.of(1960, 7, 5),
                    "MALE"
            );

            doNothing().when(emailService).sendCode(anyString(), anyString());

            memberService.signUpMember(request);
            Member member = memberRepository.findByEmail(request.email()).orElseThrow();

            assertThat(member.getNickname()).isEqualTo(request.nickname());
            assertThat(member.getBirth()).isEqualTo(request.birth());
            assertThat(member.getEmail()).isEqualTo(request.email());
        }

        @Test
        void 중복된_이메일일_떄_409_반환() {
            Member member = testDataFactory.createTester();

            SignUpRequest request = new SignUpRequest(
                    "인디아나 존스",
                    "rawPassword",
                    "johns",
                    member.getEmail(),
                    LocalDate.of(1960, 7, 5),
                    "MALE"
            );

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
            Member member = testDataFactory.createTester();

            SignUpRequest request = new SignUpRequest(
                    "인디아나 존스",
                    "rawPassword",
                    member.getNickname(),
                    "indiana@johns.com",
                    LocalDate.of(1960, 7, 5),
                    "MALE"
            );

            assertThatThrownBy(() -> memberService.signUpMember(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_NICKNAME);
                        assertThat(ce.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
                    });
        }
    }

    @Nested
    class 닉네임_변경_테스트 {

        @Test
        void 닉네임_정상_변경_시_id_반환() {
            Member member = testDataFactory.createTester();

            NicknameUpdateRequest request = new NicknameUpdateRequest("수정된 테스터");

            MemberIdResponse response = memberService.updateNickname(member.getId(), request);

            Member findMember = memberRepository.findById(response.memberId())
                    .orElseThrow();

            assertThat(findMember.getNickname()).isEqualTo(request.nickname());
        }

        @Test
        void 닉네임_중복_시_409_반환() {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();

            NicknameUpdateRequest request = new NicknameUpdateRequest(faker.getNickname());

            assertThatThrownBy(() -> memberService.updateNickname(tester.getId(), request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_NICKNAME);
                        assertThat(ce.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
                    });
        }

        @Test
        void 로그인된_id로_회원이_없을_시_404_반환() {
            Member tester = testDataFactory.createTester();

            NicknameUpdateRequest request = new NicknameUpdateRequest("닐리리랄라");

            assertThatThrownBy(() -> memberService.updateNickname(43214321L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_MEMBER);
                        assertThat(ce.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

    @Nested
    class 비밀번호_변경_테스트 {

        @Test
        void 비밀번호_정상_변경_시_id_반환() {
            Member member = testDataFactory.createTester();

            PasswordUpdateRequest request = new PasswordUpdateRequest("changePassword");

            MemberIdResponse response = memberService.updatePassword(member.getId(), request);

            Member findMember = memberRepository.findById(response.memberId())
                    .orElseThrow();

            assertThat(passwordEncoder.matches(request.password(), findMember.getPassword())).isTrue();
        }

        @Test
        void 로그인된_id로_회원이_없을_시_404_반환() {
            Member tester = testDataFactory.createTester();

            PasswordUpdateRequest request = new PasswordUpdateRequest("aaabbbccc");

            assertThatThrownBy(() -> memberService.updatePassword(43214321L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_MEMBER);
                        assertThat(ce.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

    @Nested
    class 회원_탈퇴_테스트 {

        @Test
        void 회원_정상_탈퇴_시_회원_상태_변경() {
            Member tester = testDataFactory.createTester();

            memberService.deleteMember(tester.getId());

            Member findTester = memberRepository.findById(tester.getId())
                    .orElseThrow();

            assertThat(findTester.getActivation()).isEqualTo(Activation.WAITING_DELETED);
            assertThat(findTester.getDeletedAt()).isNotNull();
        }

        @Test
        void 로그인된_id로_회원이_없을_시_404_반환() {
            Member tester = testDataFactory.createTester();

            assertThatThrownBy(() -> memberService.deleteMember(43214321L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_MEMBER);
                        assertThat(ce.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

    @Nested
    class 회원_정보_공개범위_수정_테스트 {

        @Test
        void 회원_정보_공개범위_수정_정상_시_id_반환() {
            Member tester = testDataFactory.createTester();

            PrivacyUpdateRequest request = new PrivacyUpdateRequest("PRIVATE");

            MemberIdResponse response = memberService.updatePrivacy(tester.getId(), request);

            Member findMember = memberRepository.findById(response.memberId())
                    .orElseThrow();

            assertThat(findMember.getVisibility()).isEqualTo(Visibility.PRIVATE);
        }

        @Test
        void 로그인된_id로_회원이_없을_시_404_반환() {
            Member tester = testDataFactory.createTester();

            PrivacyUpdateRequest request = new PrivacyUpdateRequest("PRIVATE");

            assertThatThrownBy(() -> memberService.updatePrivacy(43214321L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_MEMBER);
                        assertThat(ce.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

    @Nested
    class 비밀번호_초기화_테스트 {

        @Test
        void 비밀번호_정상_초기화() {
            Member tester = testDataFactory.createTester();

            PasswordResetRequest request = new PasswordResetRequest(tester.getName(), tester.getEmail(), tester.getBirth());

            doNothing().when(emailService).sendTempPassword(anyString(), anyString());

            memberService.resetPassword(request);

            Member findMember = memberRepository.findById(tester.getId())
                    .orElseThrow();

            assertThat(passwordEncoder.matches("password", findMember.getPassword())).isFalse();
        }

        @Test
        void 이메일로_가입_되어있지_않으면_404반환() {
            Member tester = testDataFactory.createTester();

            PasswordResetRequest request = new PasswordResetRequest(tester.getName(), "email@asdf.com", tester.getBirth());

            assertThatThrownBy(() -> memberService.resetPassword(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_MEMBER);
                        assertThat(ce.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }

        @Test
        void 이메일은_맞지만_생년월일이_다르면_404반환() {
            Member tester = testDataFactory.createTester();

            PasswordResetRequest request = new PasswordResetRequest(tester.getName(), tester.getEmail(), LocalDate.of(1955, 6, 6));

            assertThatThrownBy(() -> memberService.resetPassword(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_MEMBER);
                        assertThat(ce.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }

        @Test
        void 이메일도_맞고_생년월일도_맞지만_이름이_다르면_404반환() {
            Member tester = testDataFactory.createTester();

            PasswordResetRequest request = new PasswordResetRequest("테스텅", tester.getEmail(), tester.getBirth());

            assertThatThrownBy(() -> memberService.resetPassword(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_MEMBER);
                        assertThat(ce.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

    @Nested
    class 회원_탈퇴_취소_테스트 {

        @Test
        void 회원_탈퇴_정상_취소() {
            Member tester = testDataFactory.createTester();
            tester.deleteMember();

            Member deletedMember = memberRepository.findById(tester.getId())
                    .orElseThrow();

            assertThat(deletedMember.getDeletedAt()).isNotNull();
            assertThat(deletedMember.getActivation()).isEqualTo(Activation.WAITING_DELETED);

            CancelDeleteRequest request = new CancelDeleteRequest(
                    tester.getName(),
                    "password",
                    tester.getEmail(),
                    tester.getBirth()
            );

            memberService.cancelDeleteMember(request);

            Member cancelMember = memberRepository.findById(tester.getId())
                    .orElseThrow();

            assertThat(cancelMember.getDeletedAt()).isNull();
            assertThat(cancelMember.getActivation()).isEqualTo(Activation.ACTIVE);
        }

        @Test
        void 회원_탈퇴_상태가_아니면_400_반환() {
            Member tester = testDataFactory.createTester();
            tester.stopActivity();

            CancelDeleteRequest request = new CancelDeleteRequest(
                    tester.getName(),
                    "password",
                    tester.getEmail(),
                    tester.getBirth()
            );

            Member stopMember = memberRepository.findById(tester.getId())
                    .orElseThrow();

            assertThat(stopMember.getActivation()).isEqualTo(Activation.SUSPENDED);

            assertThatThrownBy(() -> memberService.cancelDeleteMember(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    });
        }

        @Test
        void 비밀번호_틀릴_시_401_반환() {
            Member tester = testDataFactory.createTester();
            tester.deleteMember();

            Member deletedMember = memberRepository.findById(tester.getId())
                    .orElseThrow();

            assertThat(deletedMember.getDeletedAt()).isNotNull();
            assertThat(deletedMember.getActivation()).isEqualTo(Activation.WAITING_DELETED);

            CancelDeleteRequest request = new CancelDeleteRequest(
                    tester.getName(),
                    "password1234",
                    tester.getEmail(),
                    tester.getBirth()
            );

            assertThatThrownBy(() -> memberService.cancelDeleteMember(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    });
        }

        @Test
        void 생년월일이나_이름_틀릴_시_404_반환() {
            Member tester = testDataFactory.createTester();
            tester.deleteMember();

            Member deletedMember = memberRepository.findById(tester.getId())
                    .orElseThrow();

            assertThat(deletedMember.getDeletedAt()).isNotNull();
            assertThat(deletedMember.getActivation()).isEqualTo(Activation.WAITING_DELETED);

            CancelDeleteRequest request = new CancelDeleteRequest(
                    tester.getName(),
                    "password",
                    tester.getEmail(),
                    tester.getBirth().plusDays(1)
            );

            assertThatThrownBy(() -> memberService.cancelDeleteMember(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_MEMBER);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

    @Nested
    class 인증_코드_확인_테스트 {

        @Test
        void 인증_코드_정상_시_Role_변경() {
            Member guest = testDataFactory.createGuest();

            String redisKey = "CODE:" + guest.getEmail();
            String redisValue = "a1b2c3d4e5";

            redisTemplate.opsForValue().set(redisKey, redisValue, Duration.ofMinutes(5));

            CodeCheckRequest request = new CodeCheckRequest(guest.getEmail(), redisValue);

            memberService.checkCode(request);

            Member findMember = memberRepository.findById(guest.getId())
                    .orElseThrow();

            assertThat(findMember.getRole()).isEqualTo(Role.MEMBER);
        }

        @Test
        void 등록된_회원이_없을_시_401_반환() {
            Member guest = testDataFactory.createGuest();

            String redisKey = "CODE:" + guest.getEmail();
            String redisValue = "a1b2c3d4e5";

            redisTemplate.opsForValue().set(redisKey, redisValue, Duration.ofMinutes(5));

            CodeCheckRequest request = new CodeCheckRequest("arararar@email.com", redisValue);

            assertThatThrownBy(() -> memberService.checkCode(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_CODE);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    });
        }

        @Test
        void 인증_코드가_아니거나_저장된_코드가_없을_시_401_반환() {
            Member guest = testDataFactory.createGuest();

            String redisKey = "CODE:" + guest.getEmail();
            String redisValue = "a1b2c3d4e5";

            redisTemplate.opsForValue().set(redisKey, redisValue, Duration.ofMinutes(5));

            CodeCheckRequest request = new CodeCheckRequest(guest.getEmail(), "a1b2c3d4");

            assertThatThrownBy(() -> memberService.checkCode(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_CODE);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    });
        }

        @Test
        void 이미_인증된_회원일_시_409_반환() {
            Member dancer = testDataFactory.createDancer();

            String redisKey = "CODE:" + dancer.getEmail();
            String redisValue = "a1b2c3d4e5";

            redisTemplate.opsForValue().set(redisKey, redisValue, Duration.ofMinutes(5));

            CodeCheckRequest request = new CodeCheckRequest(dancer.getEmail(), redisValue);

            assertThatThrownBy(() -> memberService.checkCode(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ALREADY_AUTHENTICATED_MEMBER);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
                    });
        }
    }

    @Nested
    class 인증_코드_재전송_테스트 {

        @Test
        void 인증_코드_재전송_정상() {
            Member guest = testDataFactory.createGuest();

            CodeReSendRequest request = new CodeReSendRequest(guest.getEmail());

            doNothing().when(emailService).sendCode(anyString(), anyString());

            memberService.reSendCode(request);

            verify(emailService, times(1)).sendCode(eq(guest.getEmail()), anyString());
        }

        @Test
        void 이미_인증_회원_시_전송하지_않음() {
            Member dancer = testDataFactory.createDancer();
            CodeReSendRequest request = new CodeReSendRequest(dancer.getEmail());
            memberService.reSendCode(request);

            verify(emailService, never()).sendCode(anyString(), anyString());
        }
    }
}
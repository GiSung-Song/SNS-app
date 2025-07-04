package com.outsta.sns.domain.member.service;

import com.outsta.sns.common.error.CustomException;
import com.outsta.sns.common.error.ErrorCode;
import com.outsta.sns.domain.enums.Activation;
import com.outsta.sns.domain.enums.Gender;
import com.outsta.sns.domain.enums.Role;
import com.outsta.sns.domain.enums.Visibility;
import com.outsta.sns.domain.member.dto.request.*;
import com.outsta.sns.domain.member.dto.response.CheckEmailResponse;
import com.outsta.sns.domain.member.dto.response.CheckNicknameResponse;
import com.outsta.sns.domain.member.dto.response.MemberIdResponse;
import com.outsta.sns.domain.member.email.EmailService;
import com.outsta.sns.domain.member.entity.Member;
import com.outsta.sns.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;

/**
 * 회원 관련 서비스
 *
 * <p>회원가입, 중복체크(닉네임, 이메일), 수정(닉네임, 이메일) 등 기능 제공</p>
 */
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String CHAR_POOL = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int VALUE_LENGTH = 10;
    private final SecureRandom random = new SecureRandom();

    /**
     * 회원 가입
     * - 닉네임, 이메일 중복 체크
     * - 정상 저장 시 이메일로 인증코드 전송
     *
     * @param request 회원가입 Request DTO (이메일, 닉네임, 비밀번호, 생년월일, 성별, 이름)
     * @throws CustomException 중복된 이메일, 닉네임 혹은 데이터 중복 오류 시 발생
     */
    @Transactional
    public void signUpMember(SignUpRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        if (memberRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        try {
            Member member = Member.builder()
                    .name(request.name())
                    .password(passwordEncoder.encode(request.password()))
                    .nickname(request.nickname())
                    .email(request.email())
                    .birth(request.birth())
                    .gender(Gender.valueOf(request.gender()))
                    .build();

            memberRepository.save(member).getId();

            String code = generateRandomValue();
            emailService.sendCode(request.email(), code);
        } catch (DataIntegrityViolationException e) {
            if (StringUtils.hasText(e.getMessage())) {
                if (e.getMessage().contains("unique_member_email")) {
                    throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
                } else if (e.getMessage().contains("unique_member_nickname")) {
                    throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
                }
            }
            throw new CustomException(ErrorCode.DUPLICATE_MEMBER);
        }
    }

    /**
     * 닉네임 중복 체크
     *
     * @param nickname 닉네임
     * @return 중복되면 true, 아니면 false
     */
    @Transactional(readOnly = true)
    public CheckNicknameResponse checkDuplicateNickname(String nickname) {
        boolean exists = memberRepository.existsByNickname(nickname);

        return new CheckNicknameResponse(exists);
    }

    /**
     * 이메일 중복 체크
     * @param email 이메일
     * @return 중복되면 true, 아니면 false
     */
    @Transactional(readOnly = true)
    public CheckEmailResponse checkDuplicateEmail(String email) {
        boolean exists = memberRepository.existsByEmail(email);

        return new CheckEmailResponse(exists);
    }

    /**
     * 닉네임 수정
     * - 닉네임 중복 체크
     * @param memberId 로그인한 회원 식별자 ID
     * @param request  변경하려고 하는 닉네임 Request DTO
     * @return 회원 식별자 ID를 포함한 Response
     */
    @Transactional
    public MemberIdResponse updateNickname(Long memberId, NicknameUpdateRequest request) {
        if (memberRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        Member member = findMemberById(memberId);

        member.updateNickname(request.nickname());

        return new MemberIdResponse(memberId);
    }

    /**
     * 비밀번호 수정
     * @param memberId 로그인한 회원 식별자 ID
     * @param request  변경하려고 하는 비밀번호 Request DTO
     * @return 회원 식별자 ID를 포함한 Response
     */
    @Transactional
    public MemberIdResponse updatePassword(Long memberId, PasswordUpdateRequest request) {
        Member member = findMemberById(memberId);

        member.updatePassword(passwordEncoder.encode(request.password()));

        return new MemberIdResponse(memberId);
    }

    /**
     * 회원 탈퇴
     * @param memberId 로그인한 회원 식별자 ID
     */
    @Transactional
    public void deleteMember(Long memberId) {
        Member member = findMemberById(memberId);

        member.deleteMember();
    }

    /**
     * 프로필 정보 공개 범위 설정
     *
     * @param memberId 로그인한 회원 식별자 ID
     * @param request  프로필 정보 공개 범위
     * @return 회원 식별자 ID를 포함한 Response
     */
    @Transactional
    public MemberIdResponse updatePrivacy(Long memberId, PrivacyUpdateRequest request) {
        Member member = findMemberById(memberId);

        member.updatePrivacy(Visibility.valueOf(request.visibility()));

        return new MemberIdResponse(memberId);
    }

    /**
     * 비밀번호 초기화
     * - 요청받은 이메일로 활성화된 회원 조회
     * - 해당 회원과 생년월일 및 이름 일치 여부 확인
     * - 일치할 시 임시 비밀번호 설정 및 해당 이메일로 임시 비밀번호 전송
     *
     * @param request 회원정보를 판단할 Request DTO (이메일, 이름, 생년월일)
     */
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        Member member = memberRepository.findActiveMemberByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        if (!member.getBirth().equals(request.birth())) {
            throw new CustomException(ErrorCode.NOT_FOUND_MEMBER);
        }

        if (!member.getName().equalsIgnoreCase(request.name())) {
            throw new CustomException(ErrorCode.NOT_FOUND_MEMBER);
        }

        String tempPassword = generateRandomValue();
        member.updatePassword(passwordEncoder.encode(tempPassword));

        emailService.sendTempPassword(request.email(), tempPassword);
    }

    /**
     * 회원 탈퇴 취소 처리
     * - 이름 이메일 비밀번호 생년월일 모두 일치해야 취소 처리
     * - 회원 상태 활성화로 변경
     *
     * @param request (이름, 이메일, 비밀번호, 생년월일)
     * @throws CustomException 이름, 이메일, 비밀번호, 생년월일 하나라도 틀릴 시 발생
     */
    @Transactional
    public void cancelDeleteMember(CancelDeleteRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        if (member.getActivation() != Activation.WAITING_DELETED) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        if (!member.getBirth().equals(request.birth())) {
            throw new CustomException(ErrorCode.NOT_FOUND_MEMBER);
        }

        if (!member.getName().equalsIgnoreCase(request.name())) {
            throw new CustomException(ErrorCode.NOT_FOUND_MEMBER);
        }

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        member.cancelDeleteMember();
    }

    /**
     * 인증 코드 확인 처리
     * - Redis에서 요청받은 이메일로 코드 확인
     * - 코드가 맞으면 Role 변경
     *
     * @param request 인증코드 Request DTO (이메일, 인증코드)
     * @throws CustomException 인증코드가 없거나, 틀리거나, 회원이 없거나, Role이 Guest가 아니면 발생
     */
    public void checkCode(CodeCheckRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CODE));

        if (member.getRole() != Role.GUEST) {
            throw new CustomException(ErrorCode.ALREADY_AUTHENTICATED_MEMBER);
        }

        String redisKey = "CODE:" + request.email();
        String savedCode = redisTemplate.opsForValue().get(redisKey);

        if (!StringUtils.hasText(savedCode) || !request.code().equals(savedCode)) {
            throw new CustomException(ErrorCode.INVALID_CODE);
        }

        member.updateRole();
    }

    /**
     * 인증 코드 재전송 처리
     * - 해당 이메일로 등록되어있지 않아도 보냈다고 처리
     * - 해당 이메일로 인증코드 전송
     *
     * @param request 인증 코드 Request DTO (이메일)
     */
    public void reSendCode(CodeReSendRequest request) {
        Member member = memberRepository.findActiveMemberByEmail(request.email())
                .orElse(null);

        if (member != null && member.getRole() == Role.GUEST) {
            String code = generateRandomValue();
            emailService.sendCode(member.getEmail(), code);
        }
    }

    //TODO : 회원 정보 조회
    @Transactional(readOnly = true)
    public void getMemberInfo(Long memberId) {
    }

    /**
     * 회원 식별자 ID로 회원 조회
     * @param memberId 회원 식별자 ID
     * @return 회원
     * @throws CustomException 해당 회원이 없을 시 발생
     */
    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
    }

    /**
     * 랜덤으로 10자리(대소문자, 숫자 포함) 문자열 생성
     * - 인증 코드 및 임시 비밀번호 설정 시 필요
     *
     * @return 임의의 10자리 문자열
     */
    private String generateRandomValue() {
        StringBuilder sb = new StringBuilder(VALUE_LENGTH);

        for (int i = 0; i < VALUE_LENGTH; i++) {
            int idx = random.nextInt(CHAR_POOL.length());

            sb.append(CHAR_POOL.charAt(idx));
        }

        return sb.toString();
    }
}

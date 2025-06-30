package com.outsta.sns.domain.member.service;

import com.outsta.sns.common.error.CustomException;
import com.outsta.sns.common.error.ErrorCode;
import com.outsta.sns.domain.enums.Gender;
import com.outsta.sns.domain.member.dto.repository.MemberRepository;
import com.outsta.sns.domain.member.dto.request.CheckEmailRequest;
import com.outsta.sns.domain.member.dto.request.CheckNicknameRequest;
import com.outsta.sns.domain.member.dto.request.SignUpRequest;
import com.outsta.sns.domain.member.dto.response.CheckEmailResponse;
import com.outsta.sns.domain.member.dto.response.CheckNicknameResponse;
import com.outsta.sns.domain.member.dto.response.MemberIdResponse;
import com.outsta.sns.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberIdResponse signUpMember(SignUpRequest request) {
        try {
            Member member = Member.builder()
                    .name(request.getName())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .nickname(request.getNickname())
                    .email(request.getEmail())
                    .birth(request.getBirth())
                    .gender(Gender.valueOf(request.getGender()))
                    .build();

            Long memberId = memberRepository.save(member).getId();

            return new MemberIdResponse(memberId);
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("unique_members_email")) {
                throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
            } else if (e.getMessage().contains("unique_members_nickname")) {
                throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
            }

            throw new CustomException(ErrorCode.DUPLICATE_MEMBER);
        }
    }

    @Transactional(readOnly = true)
    public CheckEmailResponse checkDuplicateEmail(CheckEmailRequest request) {
        boolean exists = memberRepository.existsByEmail(request.getEmail());

        return new CheckEmailResponse(exists);
    }

    @Transactional(readOnly = true)
    public CheckNicknameResponse checkDuplicateNickname(CheckNicknameRequest request) {
        boolean exists = memberRepository.existsByNickname(request.getNickname());

        return new CheckNicknameResponse(exists);
    }
}

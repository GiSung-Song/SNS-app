package com.outsta.sns.domain.member.service;

import com.outsta.sns.common.error.CustomException;
import com.outsta.sns.common.error.ErrorCode;
import com.outsta.sns.domain.member.dto.response.util.MemberAccessCheckDto;
import com.outsta.sns.domain.member.entity.Member;
import com.outsta.sns.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 회원 조회 관련 공통 서비스
 *
 * <p>식별자 ID로 회원 조회, 활성화 상태인 회원 조회</p>
 */
@Component
@RequiredArgsConstructor
public class MemberUtilService {

    private final MemberRepository memberRepository;

    /**
     * 회원 식별자 ID로 회원 조회
     * @param memberId 회원 식별자 ID
     * @return 회원
     * @throws CustomException 해당 회원이 없을 시 발생
     */
    public Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
    }

    /**
     * 회원 식별자 ID로 활성화 상태의 회원 조회
     * @param memberId 회원 식별자 ID
     * @return Member 회원 객체
     * @throws CustomException 해당 회원이 없을 시 발생
     */
    public Member findActiveMemberById(Long memberId) {
        return memberRepository.findActiveMemberById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
    }

    /**
     * 회원 식별자 ID로 활성화 상태의 회원 조회
     * - 회원 접근 권한 체크용
     *
     * @param memberId 회원 식별자 ID
     * @return MemberAccessCheckDto 회원 식별자 ID, 회원 정보 공개 범위
     * @throws CustomException 해당 회원이 없을 시 발생
     */
    public MemberAccessCheckDto getActiveMemberFollow(Long memberId) {
        Member member = memberRepository.findActiveMemberById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        return MemberAccessCheckDto.from(member);
    }
}

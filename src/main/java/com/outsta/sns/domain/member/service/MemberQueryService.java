package com.outsta.sns.domain.member.service;

import com.outsta.sns.common.error.CustomException;
import com.outsta.sns.common.error.ErrorCode;
import com.outsta.sns.domain.follow.dto.FollowerCountDto;
import com.outsta.sns.domain.follow.dto.FollowingCountDto;
import com.outsta.sns.domain.follow.service.FollowService;
import com.outsta.sns.domain.member.access.AccessPolicy;
import com.outsta.sns.domain.member.dto.response.MemberInfoResponse;
import com.outsta.sns.domain.member.entity.Member;
import com.outsta.sns.domain.member.repository.MemberRepository;
import com.outsta.sns.domain.profile.dto.response.RepresentImageDto;
import com.outsta.sns.domain.profile.service.ProfileImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 조회 관련 서비스
 *
 * <p>회원 상세조회 기능</p>
 */
@Service
@RequiredArgsConstructor
public class MemberQueryService {

    private final MemberRepository memberRepository;
    private final FollowService followService;
    private final AccessPolicy accessPolicy;
    private final ProfileImageService profileImageService;

    /**
     * 자신의 상세 정보 조회
     *
     * @param loginId 로그인한 회원의 식별자 ID
     * @return 회원 정보 + 대표 프로필 이미지(없으면 최신 프로필 이미지) + 팔로워 / 팔로잉 수
     */
    @Transactional(readOnly = true)
    public MemberInfoResponse getMyInfo(Long loginId) {
        Member member = memberRepository.findById(loginId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        return from(member);
    }

    /**
     * 자신의 상세 정보 조회
     *
     * @param loginId  로그인한 회원의 식별자 ID
     * @param memberId 조회하려는 회원의 식별자 ID
     * @return 회원 정보 + 대표 프로필 이미지(없으면 최신 프로필 이미지) + 팔로워 / 팔로잉 수
     */
    @Transactional(readOnly = true)
    public MemberInfoResponse getMemberInfo(Long loginId, Long memberId) {
        Member member = memberRepository.findActiveMemberById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        accessPolicy.checkVisibilityAndBlock(loginId, memberId, member.getVisibility());

        return from(member);
    }

    private MemberInfoResponse from(Member member) {
        FollowerCountDto followerCount = followService.getFollowerCount(member.getId());
        FollowingCountDto followingCount = followService.getFollowingCount(member.getId());
        RepresentImageDto representImage = profileImageService.getRepresentImage(member.getId());

        return new MemberInfoResponse(
                member.getId(),
                member.getName(),
                member.getNickname(),
                member.getBirth(),
                member.getGender().getCode(),
                member.getGender().getValue(),
                followerCount.count(),
                followingCount.count(),
                representImage != null ? representImage.profileImageId() : null,
                representImage != null ? representImage.imageUrl() : null,
                representImage != null ? representImage.originName() : null,
                representImage != null ? representImage.fileName() : null
        );
    }
}

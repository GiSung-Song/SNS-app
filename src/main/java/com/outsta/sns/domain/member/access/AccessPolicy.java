package com.outsta.sns.domain.member.access;

import com.outsta.sns.common.error.CustomException;
import com.outsta.sns.common.error.ErrorCode;
import com.outsta.sns.domain.block.service.BlockFollowRelationService;
import com.outsta.sns.domain.enums.Visibility;
import com.outsta.sns.domain.follow.repository.FollowQueryRepository;
import com.outsta.sns.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AccessPolicy {

    private final FollowQueryRepository followQueryRepository;
    private final BlockFollowRelationService blockFollowRelationService;

    /**
     * 회원의 정보 공개 범위에 따른 권한 체크
     * 차단 여부로 인해 조회 권한 체크
     * 공통 로직
     *
     * @param loginId 현재 로그인한 회원의 식별자 ID
     * @param member  조회 하려는 회원
     */
    public void checkVisibilityAndBlock(Long loginId, Member member) {
        // 차단 여부 확인
        if (loginId != null && blockFollowRelationService.isBlockedWhoever(loginId, member.getId())) {
            throw new CustomException(ErrorCode.BLOCK_MEMBER);
        }

        Visibility visibility = member.getVisibility();

        // 비공개
        if (visibility == Visibility.PRIVATE) {
            throw new CustomException(ErrorCode.VISIBILITY_PRIVATE);
        }

        // 팔로워 전용
        if (visibility == Visibility.FOLLOWER_ONLY) {
            if (loginId == null || !followQueryRepository.existsByLoginIdAndMemberId(loginId, member.getId())) {
                throw new CustomException(ErrorCode.VISIBILITY_FOLLOWER_ONLY);
            }
        }
    }
}

package com.outsta.sns.domain.block.service;

import com.outsta.sns.domain.block.repository.BlockQueryRepository;
import com.outsta.sns.domain.follow.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 차단과 팔로우의 연관된 공통 로직 서비스
 */
@Service
@RequiredArgsConstructor
public class BlockFollowRelationService {

    private final FollowRepository followRepository;
    private final BlockQueryRepository blockQueryRepository;

    /**
     * 어느 한쪽이 차단했는지 여부
     *
     * @param loginId  로그인한 사용자의 식별자 ID
     * @param memberId 확인하려는 대상의 식별자 ID
     * @return 어느 한쪽이라도 차단했으면 true, 아니면 false
     */
    public boolean isBlockedWhoever(Long loginId, Long memberId) {
        return blockQueryRepository.existsBlockWhoever(loginId, memberId);
    }

    /**
     * 팔로우나 팔로잉되어있으면 삭제
     *
     * @param loginId  로그인한 사용자의 식별자 ID
     * @param memberId 팔로우나 팔로잉에서 삭제하려는 대상의 식별자 ID
     */
    public void deleteFollowRelation(Long loginId, Long memberId) {
        followRepository.deleteFollow(loginId, memberId);
    }

}

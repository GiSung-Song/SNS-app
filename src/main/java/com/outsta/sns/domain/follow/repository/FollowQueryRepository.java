package com.outsta.sns.domain.follow.repository;

import com.outsta.sns.domain.follow.dto.FollowerListResponse;
import com.outsta.sns.domain.follow.dto.FollowingListResponse;
import com.outsta.sns.domain.follow.entity.QFollow;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 팔로우 엔티티 조회용 커스텀 쿼리
 */
@Repository
@RequiredArgsConstructor
public class FollowQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    /**
     * 이미 팔로우한 회원인지 체크
     *
     * @param loginId  로그인한 회원의 식별자 ID
     * @param memberId 차단하려고 하는 회원의 식별자 ID
     * @return 팔로우되어 있으면 true, 아니면 false 반환
     */
    public boolean existsByLoginIdAndMemberId(Long loginId, Long memberId) {
        QFollow follow = QFollow.follow;

        return jpaQueryFactory
                .selectOne()
                .from(follow)
                .where(
                        follow.follower.id.eq(loginId),
                        follow.following.id.eq(memberId)
                )
                .fetchFirst() != null;
    }

    /**
     * 팔로워 목록 조회
     * @param memberId 회원 식별자 ID
     * @return 회원의 팔로워 목록
     */
    public List<FollowerListResponse.FollowerMemberDto> getFollowerList(Long memberId) {
        QFollow follow = QFollow.follow;

        return jpaQueryFactory
                .select(Projections.constructor(
                        FollowerListResponse.FollowerMemberDto.class,
                        follow.follower.id,
                        follow.follower.nickname

                ))
                .from(follow)
                .where(follow.following.id.eq(memberId))
                .fetch();
    }

    /**
     * 팔로잉 목록 조회
     * @param memberId 회원 식별자 ID
     * @return 회원의 팔로잉 목록
     */
    public List<FollowingListResponse.FollowingMemberDto> getFollowingList(Long memberId) {
        QFollow follow = QFollow.follow;

        return jpaQueryFactory
                .select(Projections.constructor(
                        FollowingListResponse.FollowingMemberDto.class,
                        follow.following.id,
                        follow.following.nickname

                ))
                .from(follow)
                .where(follow.follower.id.eq(memberId))
                .fetch();
    }

}

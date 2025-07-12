package com.outsta.sns.domain.block.repository;

import com.outsta.sns.domain.block.dto.BlockListResponse;
import com.outsta.sns.domain.block.entity.QBlock;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 차단 엔티티 조회용 커스텀 쿼리
 */
@Repository
@RequiredArgsConstructor
public class BlockQueryRepository {
    private final JPAQueryFactory queryFactory;
    private final QBlock block = QBlock.block;

    /**
     * 이미 차단한 회원인지 체크
     *
     * @param loginId  로그인한 회원의 식별자 ID
     * @param memberId 차단하려고 하는 회원의 식별자 ID
     * @return 차단 여부
     */
    public boolean existsByLoginIdAndMemberId(Long loginId, Long memberId) {
        return queryFactory
                .selectOne()
                .from(block)
                .where(
                        block.blocker.id.eq(loginId),
                        block.blocked.id.eq(memberId)
                )
                .fetchFirst() != null;
    }

    /**
     * 차단한 회원 목록 조회 (회원 식별자 ID, 닉네임)
     *
     * @param loginId 로그인한 회원의 식별자 ID
     * @return List<BlockListResponse.BlockMemberDto> 차단한 회원 목록 (회원 식별자 ID, 닉네임)
     */
    public List<BlockListResponse.BlockMemberDto> getBlockedMemberList(Long loginId) {
        return queryFactory
                .select(Projections.constructor(
                        BlockListResponse.BlockMemberDto.class,
                        block.blocked.id,
                        block.blocked.nickname
                ))
                .from(block)
                .where(block.blocker.id.eq(loginId))
                .fetch();
    }

    /**
     * 자신과 회원이 차단했는지 여부 체크
     * - 자신이 회원을 차단했던지 회원이 자신을 차단했던지 체크
     * @param loginId  로그인한 회원의 식별자 ID
     * @param memberId 차단 여부를 판단할 회원의 식별자 ID
     * @return 한쪽이라도 차단했으면 true, 아니면 false 반환
     */
    public boolean existsBlockWhoever(Long loginId, Long memberId) {
        return queryFactory
                .selectOne()
                .from(block)
                .where(
                        block.blocker.id.eq(loginId).and(block.blocked.id.eq(memberId))
                                .or(block.blocker.id.eq(memberId).and(block.blocked.id.eq(loginId)))
                )
                .fetchFirst() != null;
    }
}

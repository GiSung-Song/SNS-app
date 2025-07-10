package com.outsta.sns.domain.member.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 회원 엔티티 조회용 커스텀 쿼리
 */
@RequiredArgsConstructor
@Repository
public class MemberQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;
}
package com.outsta.sns.domain.profile.repository;

import com.outsta.sns.domain.profile.entity.QProfileImage;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProfileImageQueryRepository {
    private final JPAQueryFactory queryFactory;

    public boolean existsMyProfileImage(Long loginId, Long imageId) {
        QProfileImage profileImage = QProfileImage.profileImage;

        return queryFactory
                .selectOne()
                .from(profileImage)
                .where(
                        profileImage.member.id.eq(loginId),
                        profileImage.id.eq(imageId)
                )
                .fetchFirst() != null;
    }
}

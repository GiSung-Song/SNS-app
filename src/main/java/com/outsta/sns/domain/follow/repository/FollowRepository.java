package com.outsta.sns.domain.follow.repository;

import com.outsta.sns.domain.follow.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * 팔로우 엔티티에 대한 JPA 레포지토리
 */
public interface FollowRepository extends JpaRepository<Follow, Long> {

    @Modifying
    @Query("DELETE FROM Follow f WHERE (f.follower.id = :loginId AND f.following.id = :memberId) OR (f.follower.id = :memberId AND f.following.id = :loginId)")
    void deleteFollow(Long loginId, Long memberId);

    /** 차단 엔티티 조회 */
    @Query("SELECT f FROM Follow f WHERE f.follower.id = :loginId AND f.following.id = :memberId")
    Optional<Follow> findByLoginIdAndMemberId(Long loginId, Long memberId);

}
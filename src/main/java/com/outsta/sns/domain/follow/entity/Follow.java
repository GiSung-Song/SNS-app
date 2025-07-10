package com.outsta.sns.domain.follow.entity;

import com.outsta.sns.domain.BaseTimeEntity;
import com.outsta.sns.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

/**
 * 팔로우 엔티티
 * - 팔로워, 팔로잉 관리
 */
@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(name = "unique_follow", columnNames = {"follower_id", "following_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Follow extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "follower_id", nullable = false)
    private Member follower;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "following_id", nullable = false)
    private Member following;
}
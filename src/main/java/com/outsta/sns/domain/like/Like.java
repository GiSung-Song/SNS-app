package com.outsta.sns.domain.like;

import com.outsta.sns.domain.BaseTimeEntity;
import com.outsta.sns.domain.enums.LikeType;
import com.outsta.sns.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "likes",
    uniqueConstraints = @UniqueConstraint(name = "unique_likes", columnNames = {"member_id", "like_type", "target_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Like extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private LikeType likeType;

    @Column(nullable = false)
    private Long targetId;
}
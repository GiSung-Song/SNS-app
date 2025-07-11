package com.outsta.sns.domain.block.entity;

import com.outsta.sns.domain.BaseTimeEntity;
import com.outsta.sns.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

/**
 * 차단 엔티티
 * - 차단한 사람, 차단 당한 사람
 */
@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(name = "unique_block", columnNames = {"blocker_id", "blocked_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Block extends BaseTimeEntity {

    /** 차단 식별자 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 차단한 회원 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blocker_id", nullable = false)
    private Member blocker;

    /** 차단하는 대상 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blocked_id", nullable = false)
    private Member blocked;
}
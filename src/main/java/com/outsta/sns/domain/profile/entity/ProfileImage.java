package com.outsta.sns.domain.profile.entity;

import com.outsta.sns.domain.BaseTimeEntity;
import com.outsta.sns.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

/**
 * 프로필 이미지 엔티티
 */
@Entity
@Table(name = "profile_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProfileImage extends BaseTimeEntity {

    /** 프로필 이미지 식별자 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 프로필 이미지를 가지는 회원 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /** 프로필 이미지 저장 경로 */
    @Column(nullable = false, length = 300)
    private String imageUrl;

    /** 회원이 저장한 원본 파일명 */
    @Column(nullable = false, length = 100)
    private String originName;

    /** 프로필 이미지 파일명 */
    @Column(nullable = false, length = 100)
    private String fileName;

    /** 대표 이미지 여부 */
    @Column(nullable = false)
    private boolean represent;

    public void updateRepresent(boolean represent) {
        this.represent = represent;
    }
}
package com.outsta.sns.domain.profile.entity;

import com.outsta.sns.domain.BaseTimeEntity;
import com.outsta.sns.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "profile_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProfileImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 300)
    private String imageUrl;

    @Column(nullable = false, length = 100)
    private String originName;

    @Column(nullable = false, length = 100)
    private String fileName;

    @Column(nullable = false)
    private boolean represent;
}
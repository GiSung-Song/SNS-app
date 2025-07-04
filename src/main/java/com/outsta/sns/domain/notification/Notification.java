package com.outsta.sns.domain.notification;

import com.outsta.sns.domain.BaseTimeEntity;
import com.outsta.sns.domain.enums.NotificationType;
import com.outsta.sns.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private Member sender;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_id", nullable = false)
    private Member receiver;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    private Long targetId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean readYn = false;
}

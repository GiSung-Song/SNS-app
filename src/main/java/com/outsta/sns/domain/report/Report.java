package com.outsta.sns.domain.report;

import com.outsta.sns.domain.BaseTimeEntity;
import com.outsta.sns.domain.enums.ReportType;
import com.outsta.sns.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "report",
    uniqueConstraints = @UniqueConstraint(name = "unique_report", columnNames = {"member_id", "target_type", "target_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Report extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ReportType reportType;

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false, length = 100)
    private String reason;
}
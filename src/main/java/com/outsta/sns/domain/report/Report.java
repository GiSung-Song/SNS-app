package com.outsta.sns.domain.report;

import com.outsta.sns.domain.BaseTimeEntity;
import com.outsta.sns.domain.enums.ReportType;
import com.outsta.sns.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reports",
    uniqueConstraints = @UniqueConstraint(columnNames = {"reporter_id", "target_type", "target_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Report extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Member reporter;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportType reportType;

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false)
    private String reason;
}

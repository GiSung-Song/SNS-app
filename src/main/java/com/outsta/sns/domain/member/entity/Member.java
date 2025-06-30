package com.outsta.sns.domain.member.entity;

import com.outsta.sns.domain.BaseTimeEntity;
import com.outsta.sns.domain.enums.Activation;
import com.outsta.sns.domain.enums.Gender;
import com.outsta.sns.domain.enums.Role;
import com.outsta.sns.domain.enums.Visibility;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "members", uniqueConstraints = {
        @UniqueConstraint(name = "unique_members_email", columnNames = "email"),
        @UniqueConstraint(name = "unique_members_nickname", columnNames = "nickname")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(nullable = false)
    private LocalDate birth;

    private LocalDate lastStoppedDate;
    private Long stoppedCount;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    @Builder.Default
    private Role role = Role.GUEST;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    @Builder.Default
    private Activation activation = Activation.ACTIVE;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    @Builder.Default
    private Visibility visibility = Visibility.PUBLIC;
}

package com.outsta.sns.domain.member.entity;

import com.outsta.sns.domain.BaseTimeEntity;
import com.outsta.sns.domain.enums.Activation;
import com.outsta.sns.domain.enums.Gender;
import com.outsta.sns.domain.enums.Role;
import com.outsta.sns.domain.enums.Visibility;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * 회원 엔티티
 * - 회원 기본 정보 및 상태 관리
 */
@Entity
@Table(name = "member", uniqueConstraints = {
        @UniqueConstraint(name = "unique_member_email", columnNames = "email"),
        @UniqueConstraint(name = "unique_member_nickname", columnNames = "nickname")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Member extends BaseTimeEntity {

    /** 회원 식별자 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 회원 이름 (최대 50자) */
    @Column(nullable = false, length = 50)
    private String name;

    /** 비밀번호 (암호화 저장) */
    @Column(nullable = false)
    private String password;

    /** 닉네임 (중복 불가, 최대 30자) */
    @Column(nullable = false, length = 30)
    private String nickname;

    /** 이메일 (중복 불가, 최대 50자) */
    @Column(nullable = false, length = 50)
    private String email;

    /** 생년월일 */
    @Column(nullable = false)
    private LocalDate birth;

    /** 최근 정지 일자 */
    private LocalDate lastStoppedDate;

    /** 정지 횟수 */
    @Builder.Default
    private int stoppedCount = 0;

    /** 회원 권한 (기본 GUEST) */
    @Column(nullable = false, length = 20)
    @Enumerated(value = EnumType.STRING)
    @Builder.Default
    private Role role = Role.GUEST;

    /** 성별 */
    @Column(nullable = false, length = 20)
    @Enumerated(value = EnumType.STRING)
    private Gender gender;

    /** 회원 활동 상태 (활성화/정지/탈퇴 대기) */
    @Column(nullable = false, length = 20)
    @Enumerated(value = EnumType.STRING)
    @Builder.Default
    private Activation activation = Activation.ACTIVE;

    /** 프로필 공개 범위 (전체 공개/팔로워 전용/비공개) */
    @Column(nullable = false, length = 20)
    @Enumerated(value = EnumType.STRING)
    @Builder.Default
    private Visibility visibility = Visibility.PUBLIC;

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    /**
     * 회원 탈퇴 대기 처리
     * 삭제 시간을 지금 시간으로 설정
     */
    public void deleteMember() {
        this.activation = Activation.WAITING_DELETED;
        deleteNow();
    }

    /**
     * 회원 탈퇴 취소 메서드
     * - 회원 상태 활성화 처리
     * - 삭제 시간 null 설정
     */
    public void cancelDeleteMember() {
        this.activation = Activation.ACTIVE;
        cancelDelete();
    }

    public void updateRole() {
        this.role = Role.MEMBER;
    }

    public void stopActivity() {
        this.activation = Activation.SUSPENDED;
        this.lastStoppedDate = LocalDate.now();
        this.stoppedCount++;
    }

    public void updatePrivacy(Visibility visibility) {
        this.visibility = visibility;
    }
}

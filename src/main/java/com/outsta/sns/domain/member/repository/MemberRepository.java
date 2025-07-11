package com.outsta.sns.domain.member.repository;

import com.outsta.sns.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 회원 엔티티에 대한 JPA 레포지토리
 */
public interface MemberRepository extends JpaRepository<Member, Long> {

    /** 이메일 존재 여부 확인 */
    boolean existsByEmail(String email);

    /** 닉네임 존재 여부 확인 */
    boolean existsByNickname(String nickname);

    /** 이메일로 회원 조회 */
    Optional<Member> findByEmail(String email);

    /** 이메일로 활성화된 회원 조회 */
    @Query("SELECT m FROM Member m WHERE m.email = :email AND m.activation = 'ACTIVE'")
    Optional<Member> findActiveMemberByEmail(@Param("email") String email);

    /** 식별자 ID로 활성화된 회원 조회 */
    @Query("SELECT m FROM Member m WHERE m.id = :id AND m.activation = 'ACTIVE'")
    Optional<Member> findActiveMemberById(@Param("id") Long id);
}
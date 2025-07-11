package com.outsta.sns.domain.block.repository;

import com.outsta.sns.domain.block.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 차단 엔티티에 대한 JPA 레포지토리
 */
public interface BlockRepository extends JpaRepository<Block, Long> {

    /** 차단 엔티티 조회 */
    @Query("SELECT b FROM Block b WHERE b.blocker.id = :loginId AND b.blocked.id = :memberId")
    Optional<Block> findByLoginIdAndMemberId(@Param("loginId") Long loginId, @Param("memberId") Long memberId);
}

package com.outsta.sns.domain.profile.repository;

import com.outsta.sns.domain.profile.entity.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {

    @Query("SELECT pi FROM ProfileImage pi WHERE pi.member.id = :memberId AND pi.represent = true")
    Optional<ProfileImage> findRepresentImageByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT pi FROM ProfileImage pi WHERE pi.member.id = :memberId ORDER BY pi.represent DESC, pi.createdAt DESC")
    List<ProfileImage> findProfileImagesByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT pi FROM ProfileImage pi WHERE pi.member.id = :memberId AND pi.id = :imageId")
    Optional<ProfileImage> findMyProfileImage(@Param("memberId") Long memberId, @Param("imageId") Long imageId);
}
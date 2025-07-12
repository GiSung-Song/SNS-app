package com.outsta.sns.domain.profile.service;

import com.outsta.sns.common.error.CustomException;
import com.outsta.sns.common.error.ErrorCode;
import com.outsta.sns.domain.member.access.AccessPolicy;
import com.outsta.sns.domain.member.dto.response.util.MemberAccessCheckDto;
import com.outsta.sns.domain.member.entity.Member;
import com.outsta.sns.domain.member.service.MemberUtilService;
import com.outsta.sns.domain.profile.dto.request.ProfileImageRequest;
import com.outsta.sns.domain.profile.dto.response.ProfileImageResponse;
import com.outsta.sns.domain.profile.dto.response.RepresentImageDto;
import com.outsta.sns.domain.profile.entity.ProfileImage;
import com.outsta.sns.domain.profile.repository.ProfileImageQueryRepository;
import com.outsta.sns.domain.profile.repository.ProfileImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 프로필 이미지 관련 서비스
 *
 * <p>프로필 이미지 등록, 목록 조회, 삭제, 대표 프로필 설정</p>
 */
@Service
@RequiredArgsConstructor
public class ProfileImageService {

    private final ProfileImageRepository profileImageRepository;
    private final ProfileImageQueryRepository queryRepository;
    private final MemberUtilService memberUtilService;
    private final AccessPolicy accessPolicy;

    /**
     * 프로필 이미지 등록
     *
     * @param loginId 로그인한 회원의 식별자 ID
     * @param request 프로필 이미지 Request DTO (이미지 url, 원본 파일명, 이미지 파일명, 대표 프로필 이미지 여부)
     */
    @Transactional
    @CacheEvict(value = "representImage", key = "#loginId")
    public void saveProfileImage(Long loginId, ProfileImageRequest request) {
        Member member = memberUtilService.findMemberById(loginId);

        Optional<ProfileImage> currentRepresent = profileImageRepository.findRepresentImageByMemberId(loginId);

        if (request.represent()) {
            if (currentRepresent != null) {
                currentRepresent.ifPresent(pi -> pi.updateRepresent(false));
            }
        }

        ProfileImage profileImage = ProfileImage.builder()
                .member(member)
                .imageUrl(request.imageUrl())
                .originName(request.originName())
                .fileName(request.fileName())
                .represent(request.represent())
                .build();

        profileImageRepository.save(profileImage);
    }

    /**
     * 내 프로필 이미지 목록 조회
     *
     * @param loginId 로그인한 회원의 식별자 ID
     * @return
     */
    @Transactional(readOnly = true)
    public ProfileImageResponse getMyProfileImages(Long loginId) {
        List<ProfileImage> profileImageList = profileImageRepository.findProfileImagesByMemberId(loginId);

        List<ProfileImageResponse.ProfileImageDto> imageDtoList = profileImageList.stream()
                .map(ProfileImageResponse.ProfileImageDto::from)
                .collect(Collectors.toList());

        return new ProfileImageResponse(imageDtoList);
    }

    /**
     * 프로필 이미지 목록 조회
     *
     * @param loginId 로그인한 회원의 식별자 ID
     * @param memberId 조회 하려는 회원의 식별자 ID
     * @return
     */
    @Transactional(readOnly = true)
    public ProfileImageResponse getProfileImages(Long loginId, Long memberId) {
        MemberAccessCheckDto member = memberUtilService.getActiveMemberFollow(memberId);
        accessPolicy.checkVisibilityAndBlock(loginId, member.id(), member.visibility());

        List<ProfileImage> profileImageList = profileImageRepository.findProfileImagesByMemberId(memberId);

        List<ProfileImageResponse.ProfileImageDto> imageDtoList = profileImageList.stream()
                .map(ProfileImageResponse.ProfileImageDto::from)
                .collect(Collectors.toList());

        return new ProfileImageResponse(imageDtoList);
    }

    /**
     * 프로필 이미지 삭제
     *
     * @param loginId 로그인한 회원의 식별자 ID
     * @param imageId 프로필 이미지 식별자 ID
     */
    @Transactional
    @CacheEvict(value = "representImage", key = "#loginId")
    public void deleteProfileImage(Long loginId, Long imageId) {
        boolean myProfileImage = queryRepository.existsMyProfileImage(loginId, imageId);

        if (!myProfileImage) {
            throw new CustomException(ErrorCode.NOT_FOUND_PROFILE_IMAGE);
        }

        profileImageRepository.deleteById(imageId);
    }

    /**
     * 대표 이미지 설정
     *
     * @param loginId 로그인한 회원의 식별자 ID
     * @param imageId 프로필 이미지 식별자 ID
     */
    @Transactional
    @CacheEvict(value = "representImage", key = "#loginId")
    public void updateRepresentImage(Long loginId, Long imageId) {
        ProfileImage profileImage = profileImageRepository.findMyProfileImage(loginId, imageId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PROFILE_IMAGE));

        //기존 대표 프로필 이미지가 있으면 false로 변경
        Optional<ProfileImage> currentRepresent = profileImageRepository.findRepresentImageByMemberId(loginId);
        currentRepresent.ifPresent(pi -> pi.updateRepresent(false));

        profileImage.updateRepresent(true);
    }

    /**
     * 대표 이미지 조회
     * - 없을 시 최신 이미지
     * - 프로필 이미지가 아예 없으면 null
     *
     * @param memberId 회원 식별자 ID
     * @return 회원 대표 이미지 혹은 최신 이미지 혹은 null
     */
    @Cacheable(value = "representImage", key = "#memberId")
    public RepresentImageDto getRepresentImage(Long memberId) {
        return profileImageRepository.findRepresentImageByMemberId(memberId)
                .or(() -> profileImageRepository.findFirstByMemberIdOrderByCreatedAtDesc(memberId))
                .map(RepresentImageDto::from)
                .orElse(null);
    }
}

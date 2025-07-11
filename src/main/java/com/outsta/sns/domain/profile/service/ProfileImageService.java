package com.outsta.sns.domain.profile.service;

import com.outsta.sns.common.error.CustomException;
import com.outsta.sns.common.error.ErrorCode;
import com.outsta.sns.domain.member.access.AccessPolicy;
import com.outsta.sns.domain.member.entity.Member;
import com.outsta.sns.domain.member.service.MemberService;
import com.outsta.sns.domain.profile.dto.request.ProfileImageRequest;
import com.outsta.sns.domain.profile.dto.response.ProfileImageResponse;
import com.outsta.sns.domain.profile.entity.ProfileImage;
import com.outsta.sns.domain.profile.repository.ProfileImageQueryRepository;
import com.outsta.sns.domain.profile.repository.ProfileImageRepository;
import lombok.RequiredArgsConstructor;
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
    private final MemberService memberService;
    private final AccessPolicy accessPolicy;

    /**
     * 프로필 이미지 등록
     *
     * @param loginId 로그인한 회원의 식별자 ID
     * @param request 프로필 이미지 Request DTO (이미지 url, 원본 파일명, 이미지 파일명, 대표 프로필 이미지 여부)
     */
    @Transactional
    public void saveProfileImage(Long loginId, ProfileImageRequest request) {
        Member member = memberService.findMemberById(loginId);

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
        Member member = memberService.findActiveMemberById(memberId);

        accessPolicy.checkVisibilityAndBlock(loginId, member);

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
    public void updateRepresentImage(Long loginId, Long imageId) {
        ProfileImage profileImage = profileImageRepository.findMyProfileImage(loginId, imageId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PROFILE_IMAGE));

        //기존 대표 프로필 이미지가 있으면 false로 변경
        Optional<ProfileImage> currentRepresent = profileImageRepository.findRepresentImageByMemberId(loginId);
        currentRepresent.ifPresent(pi -> pi.updateRepresent(false));

        profileImage.updateRepresent(true);
    }
}

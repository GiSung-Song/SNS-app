package com.outsta.sns.domain.profile.service;

import com.outsta.sns.common.error.CustomException;
import com.outsta.sns.common.error.ErrorCode;
import com.outsta.sns.config.support.ServiceTestSupport;
import com.outsta.sns.domain.enums.Visibility;
import com.outsta.sns.domain.member.entity.Member;
import com.outsta.sns.domain.profile.dto.request.ProfileImageRequest;
import com.outsta.sns.domain.profile.dto.response.ProfileImageResponse;
import com.outsta.sns.domain.profile.entity.ProfileImage;
import com.outsta.sns.domain.profile.repository.ProfileImageRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfileImageServiceTest extends ServiceTestSupport {

    @Autowired
    private ProfileImageService profileImageService;

    @Autowired
    private ProfileImageRepository profileImageRepository;

    @Nested
    class 프로필_이미지_등록_테스트 {

        @Test
        void 프로필_이미지_등록_정상() {
            Member tester = testDataFactory.createTester();

            ProfileImageRequest request = new ProfileImageRequest(
                    "imageUrl",
                    "originName",
                    "fileName",
                    true
            );

            profileImageService.saveProfileImage(tester.getId(), request);

            List<ProfileImage> profileImages = profileImageRepository.findProfileImagesByMemberId(tester.getId());

            assertThat(profileImages).hasSize(1);

            ProfileImage profileImage = profileImages.get(0);

            assertThat(profileImage.getImageUrl()).isEqualTo(request.imageUrl());
            assertThat(profileImage.getOriginName()).isEqualTo(request.originName());
            assertThat(profileImage.getFileName()).isEqualTo(request.fileName());
            assertThat(profileImage.isRepresent()).isEqualTo(request.represent());
        }

        @Test
        void 프로필_이미지_등록_정상_및_대표_이미지_변경() {
            Member tester = testDataFactory.createTester();

            ProfileImage savedProfileImage = testDataFactory.createProfileImage(tester, 1, true);

            ProfileImageRequest request = new ProfileImageRequest(
                    "imageUrl1234",
                    "originName1234",
                    "fileName1234",
                    true
            );

            profileImageService.saveProfileImage(tester.getId(), request);

            List<ProfileImage> profileImages = profileImageRepository.findProfileImagesByMemberId(tester.getId());

            assertThat(profileImages).hasSize(2);

            Optional<ProfileImage> old = profileImages.stream().filter(i -> i.getId().equals(savedProfileImage.getId())).findFirst();
            Optional<ProfileImage> newest = profileImages.stream().filter(i -> !i.getId().equals(savedProfileImage.getId())).findFirst();

            assertThat(old.get().isRepresent()).isFalse();
            assertThat(newest.get().isRepresent()).isTrue();
        }
    }

    @Nested
    class 내_프로필_이미지_목록_조회_테스트 {

        @Test
        void 이미지_목록_조회_정상() {
            Member tester = testDataFactory.createTester();

            ProfileImage representImage = testDataFactory.createProfileImage(tester, 1, true);
            ProfileImage profileImage2 = testDataFactory.createProfileImage(tester, 2, false);

            ProfileImageResponse myProfileImages = profileImageService.getMyProfileImages(tester.getId());

            assertThat(myProfileImages.profileImageList()).hasSize(2);
            assertThat(myProfileImages.profileImageList().get(0).profileImageId()).isEqualTo(representImage.getId());
            assertThat(myProfileImages.profileImageList().get(1).profileImageId()).isEqualTo(profileImage2.getId());
        }
    }

    @Nested
    class 프로필_이미지_목록_조회_테스트 {

        @Test
        void 이미지_목록_조회_정상() {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();

            ProfileImage representImage = testDataFactory.createProfileImage(faker, 1, true);
            ProfileImage profileImage2 = testDataFactory.createProfileImage(faker, 2, false);

            ProfileImageResponse myProfileImages = profileImageService.getProfileImages(tester.getId(), faker.getId());

            assertThat(myProfileImages.profileImageList()).hasSize(2);
            assertThat(myProfileImages.profileImageList().get(0).profileImageId()).isEqualTo(representImage.getId());
            assertThat(myProfileImages.profileImageList().get(1).profileImageId()).isEqualTo(profileImage2.getId());
        }

        @Test
        void 회원_없을_시_400_반환() {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();

            ProfileImage representImage = testDataFactory.createProfileImage(faker, 1, true);
            ProfileImage profileImage2 = testDataFactory.createProfileImage(faker, 2, false);

            assertThatThrownBy(() -> profileImageService.getProfileImages(tester.getId(), 1234321L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_MEMBER);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }

        @Test
        void 차단_했거나_차단_당했을_시_403_반환() {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();

            ProfileImage representImage = testDataFactory.createProfileImage(faker, 1, true);
            ProfileImage profileImage2 = testDataFactory.createProfileImage(faker, 2, false);

            testDataFactory.createBlock(faker, tester);

            assertThatThrownBy(() -> profileImageService.getProfileImages(tester.getId(), faker.getId()))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BLOCK_MEMBER);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    });
        }

        @Test
        void 비공개일_시_403_반환() {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();

            ProfileImage representImage = testDataFactory.createProfileImage(faker, 1, true);
            ProfileImage profileImage2 = testDataFactory.createProfileImage(faker, 2, false);

            faker.updatePrivacy(Visibility.PRIVATE);

            assertThatThrownBy(() -> profileImageService.getProfileImages(tester.getId(), faker.getId()))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.VISIBILITY_PRIVATE);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    });
        }

        @Test
        void 팔로워_전용인데_팔로워하지_않았을_경우_403_반환() {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();

            ProfileImage representImage = testDataFactory.createProfileImage(faker, 1, true);
            ProfileImage profileImage2 = testDataFactory.createProfileImage(faker, 2, false);

            faker.updatePrivacy(Visibility.FOLLOWER_ONLY);

            assertThatThrownBy(() -> profileImageService.getProfileImages(tester.getId(), faker.getId()))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.VISIBILITY_FOLLOWER_ONLY);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    });
        }

        @Test
        void 팔로워_전용인데_팔로우인_경우_정상() {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();

            ProfileImage representImage = testDataFactory.createProfileImage(faker, 1, true);
            ProfileImage profileImage2 = testDataFactory.createProfileImage(faker, 2, false);

            testDataFactory.createFollow(tester, faker);

            ProfileImageResponse myProfileImages = profileImageService.getProfileImages(tester.getId(), faker.getId());

            assertThat(myProfileImages.profileImageList()).hasSize(2);
            assertThat(myProfileImages.profileImageList().get(0).profileImageId()).isEqualTo(representImage.getId());
            assertThat(myProfileImages.profileImageList().get(1).profileImageId()).isEqualTo(profileImage2.getId());
        }
    }

    @Nested
    class 프로필_이미지_삭제_테스트 {

        @Test
        void 프로필_이미지_삭제_정상() {
            Member faker = testDataFactory.createFaker();

            ProfileImage representImage = testDataFactory.createProfileImage(faker, 1, true);
            ProfileImage profileImage2 = testDataFactory.createProfileImage(faker, 2, false);

            profileImageService.deleteProfileImage(faker.getId(), profileImage2.getId());

            List<ProfileImage> profileImages = profileImageRepository.findProfileImagesByMemberId(faker.getId());

            assertThat(profileImages).hasSize(1);
            assertThat(profileImages.get(0).getId()).isEqualTo(representImage.getId());
        }

        @Test
        void 프로필_이미지_없을_시_400_반환() {
            Member faker = testDataFactory.createFaker();
            Member dancer = testDataFactory.createDancer();

            ProfileImage dancerImage = testDataFactory.createProfileImage(dancer, 2, true);

            assertThatThrownBy(() -> profileImageService.deleteProfileImage(faker.getId(), dancerImage.getId()))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_PROFILE_IMAGE);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

    @Nested
    class 대표_이미지_설정_테스트 {

        @Test
        void 대표_이미지_설정_정상() {
            Member faker = testDataFactory.createFaker();

            ProfileImage representImage = testDataFactory.createProfileImage(faker, 1, true);
            ProfileImage profileImage2 = testDataFactory.createProfileImage(faker, 2, false);

            profileImageService.updateRepresentImage(faker.getId(), profileImage2.getId());

            ProfileImage oldRepresent = profileImageRepository.findById(representImage.getId())
                    .orElseThrow();

            ProfileImage newestRepresent = profileImageRepository.findById(profileImage2.getId())
                    .orElseThrow();

            assertThat(oldRepresent.isRepresent()).isFalse();
            assertThat(newestRepresent.isRepresent()).isTrue();
        }

        @Test
        void 프로필_이미지_없을_시_400_반환() {
            Member faker = testDataFactory.createFaker();

            ProfileImage fakerImage = testDataFactory.createProfileImage(faker, 2, true);

            assertThatThrownBy(() -> profileImageService.updateRepresentImage(faker.getId(), 43214321L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_PROFILE_IMAGE);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

}
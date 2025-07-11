package com.outsta.sns.domain.profile.controller;

import com.outsta.sns.config.support.ControllerTestSupport;
import com.outsta.sns.domain.enums.Visibility;
import com.outsta.sns.domain.member.entity.Member;
import com.outsta.sns.domain.member.repository.MemberRepository;
import com.outsta.sns.domain.profile.dto.request.ProfileImageRequest;
import com.outsta.sns.domain.profile.entity.ProfileImage;
import com.outsta.sns.domain.profile.repository.ProfileImageRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProfileImageControllerTest extends ControllerTestSupport {

    @Autowired
    private ProfileImageRepository profileImageRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Nested
    class 프로필_이미지_등록_API_테스트 {

        @Test
        void 프로필_이미지_등록_정상() throws Exception {
            Member tester = testDataFactory.createTester();
            testDataFactory.setAuthentication(tester);

            ProfileImageRequest request = new ProfileImageRequest(
                    "testURL",
                    "originName",
                    "fileName",
                    true
            );

            mockMvc.perform(post("/api/members/me/profile-images")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated());

            List<ProfileImage> profileImages = profileImageRepository.findProfileImagesByMemberId(tester.getId());

            assertThat(profileImages).hasSize(1);
            assertThat(profileImages.get(0).getImageUrl()).isEqualTo(request.imageUrl());
            assertThat(profileImages.get(0).getOriginName()).isEqualTo(request.originName());
            assertThat(profileImages.get(0).getFileName()).isEqualTo(request.fileName());
            assertThat(profileImages.get(0).isRepresent()).isEqualTo(request.represent());
        }

        @Test
        void 프로필_이미지_등록_정상_대표이미지_변경_포함() throws Exception {
            Member tester = testDataFactory.createTester();
            testDataFactory.setAuthentication(tester);

            ProfileImage savedProfileImage = testDataFactory.createProfileImage(tester, 1, true);

            ProfileImageRequest request = new ProfileImageRequest(
                    "testURL",
                    "originName",
                    "fileName",
                    true
            );

            mockMvc.perform(post("/api/members/me/profile-images")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated());

            List<ProfileImage> profileImages = profileImageRepository.findProfileImagesByMemberId(tester.getId());

            assertThat(profileImages).hasSize(2);

            Optional<ProfileImage> old = profileImages.stream().filter(i -> i.getId().equals(savedProfileImage.getId())).findFirst();
            Optional<ProfileImage> newest = profileImages.stream().filter(i -> !i.getId().equals(savedProfileImage.getId())).findFirst();

            assertThat(old.get().isRepresent()).isFalse();
            assertThat(newest.get().isRepresent()).isTrue();
        }

        @Test
        void 필수값_누락_시_400_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            testDataFactory.setAuthentication(tester);

            ProfileImageRequest request = new ProfileImageRequest(
                    "testURL",
                    "originName",
                    null,
                    true
            );

            mockMvc.perform(post("/api/members/me/profile-images")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 비로그인_시_401_반환() throws Exception {
            Member tester = testDataFactory.createTester();

            ProfileImageRequest request = new ProfileImageRequest(
                    "testURL",
                    "originName",
                    "fileName",
                    true
            );

            mockMvc.perform(post("/api/members/me/profile-images")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void 존재하지_않는_회원일_시_404_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            testDataFactory.setAuthentication(tester);

            memberRepository.delete(tester);

            ProfileImageRequest request = new ProfileImageRequest(
                    "testURL",
                    "originName",
                    "fileName",
                    true
            );

            mockMvc.perform(post("/api/members/me/profile-images")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class 프로필_이미지_목록_조회_API_테스트 {

        @Test
        void 자신의_프로필_이미지_목록_조회_성공() throws Exception {
            Member tester = testDataFactory.createTester();
            testDataFactory.setAuthentication(tester);

            ProfileImage profileImage = testDataFactory.createProfileImage(tester, 1, false);
            ProfileImage representImage = testDataFactory.createProfileImage(tester, 2, true);

            mockMvc.perform(get("/api/members/{memberId}/profile-images", tester.getId()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.profileImageList[*].profileImageId",
                            contains(representImage.getId().intValue(), profileImage.getId().intValue())));
        }

        @Test
        void 타인의_프로필_이미지_목록_조회_성공() throws Exception {
            Member tester = testDataFactory.createTester();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.setAuthentication(dancer);

            ProfileImage profileImage = testDataFactory.createProfileImage(tester, 1, false);
            ProfileImage representImage = testDataFactory.createProfileImage(tester, 2, true);

            mockMvc.perform(get("/api/members/{memberId}/profile-images", tester.getId()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.profileImageList[*].profileImageId",
                            contains(representImage.getId().intValue(), profileImage.getId().intValue())));
        }

        @Test
        void 차단했거나_차단당했을_경우_403_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.setAuthentication(dancer);

            testDataFactory.createBlock(tester, dancer);

            ProfileImage profileImage = testDataFactory.createProfileImage(tester, 1, false);
            ProfileImage representImage = testDataFactory.createProfileImage(tester, 2, true);

            mockMvc.perform(get("/api/members/{memberId}/profile-images", tester.getId()))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        void 팔로워_전용일_경우_팔로우_안했으면_403_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.setAuthentication(dancer);

            tester.updatePrivacy(Visibility.FOLLOWER_ONLY);

            ProfileImage profileImage = testDataFactory.createProfileImage(tester, 1, false);
            ProfileImage representImage = testDataFactory.createProfileImage(tester, 2, true);

            mockMvc.perform(get("/api/members/{memberId}/profile-images", tester.getId()))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        void 팔로워_전용일_때_팔로우_했으면_정상() throws Exception {
            Member tester = testDataFactory.createTester();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.setAuthentication(dancer);

            testDataFactory.createFollow(dancer, tester);

            ProfileImage profileImage = testDataFactory.createProfileImage(tester, 1, false);
            ProfileImage representImage = testDataFactory.createProfileImage(tester, 2, true);

            mockMvc.perform(get("/api/members/{memberId}/profile-images", tester.getId()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.profileImageList[*].profileImageId",
                            contains(representImage.getId().intValue(), profileImage.getId().intValue())));
        }

        @Test
        void 비공개일_경우_403_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.setAuthentication(dancer);

            tester.updatePrivacy(Visibility.PRIVATE);

            ProfileImage profileImage = testDataFactory.createProfileImage(tester, 1, false);
            ProfileImage representImage = testDataFactory.createProfileImage(tester, 2, true);

            mockMvc.perform(get("/api/members/{memberId}/profile-images", tester.getId()))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        void 존재하지_않는_회원일_경우_404_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.setAuthentication(dancer);

            ProfileImage profileImage = testDataFactory.createProfileImage(tester, 1, false);
            ProfileImage representImage = testDataFactory.createProfileImage(tester, 2, true);

            mockMvc.perform(get("/api/members/{memberId}/profile-images", 43214321L))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class 프로필_이미지_삭제_API_테스트 {

        @Test
        void 프로필_이미지_삭제_정상() throws Exception {
            Member tester = testDataFactory.createTester();
            testDataFactory.setAuthentication(tester);

            ProfileImage profileImage = testDataFactory.createProfileImage(tester, 1, false);
            ProfileImage representImage = testDataFactory.createProfileImage(tester, 2, true);

            mockMvc.perform(delete("/api/members/me/profile-images/{imageId}", profileImage.getId()))
                    .andDo(print())
                    .andExpect(status().isOk());

            ProfileImage deletedProfileImage = profileImageRepository.findById(profileImage.getId())
                    .orElse(null);

            assertThat(deletedProfileImage).isNull();
        }

        @Test
        void 유효하지_않은_경로_변수일_시_400_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            testDataFactory.setAuthentication(tester);

            ProfileImage profileImage = testDataFactory.createProfileImage(tester, 1, false);
            ProfileImage representImage = testDataFactory.createProfileImage(tester, 2, true);

            mockMvc.perform(delete("/api/members/me/profile-images/{imageId}", "profileImage"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 비로그인_시_401_반환() throws Exception {
            Member tester = testDataFactory.createTester();

            ProfileImage profileImage = testDataFactory.createProfileImage(tester, 1, false);
            ProfileImage representImage = testDataFactory.createProfileImage(tester, 2, true);

            mockMvc.perform(delete("/api/members/me/profile-images/{imageId}", profileImage.getId()))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void 존재하지_않는_프로필_이미지일_시_404_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            testDataFactory.setAuthentication(tester);

            ProfileImage profileImage = testDataFactory.createProfileImage(tester, 1, false);
            ProfileImage representImage = testDataFactory.createProfileImage(tester, 2, true);

            mockMvc.perform(delete("/api/members/me/profile-images/{imageId}", 43214321L))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class 대표_이미지_설정_API_테스트 {

        @Test
        void 대표_이미지_설정_정상() throws Exception {
            Member tester = testDataFactory.createTester();
            testDataFactory.setAuthentication(tester);

            ProfileImage profileImage = testDataFactory.createProfileImage(tester, 1, false);
            ProfileImage representImage = testDataFactory.createProfileImage(tester, 2, true);

            mockMvc.perform(patch("/api/members/me/profile-images/{imageId}", profileImage.getId()))
                    .andDo(print())
                    .andExpect(status().isOk());

            ProfileImage newRepresent = profileImageRepository.findById(profileImage.getId())
                    .orElseThrow();

            ProfileImage oldRepresent = profileImageRepository.findById(representImage.getId())
                    .orElseThrow();

            assertThat(newRepresent.isRepresent()).isTrue();
            assertThat(newRepresent.getId()).isEqualTo(profileImage.getId());

            assertThat(oldRepresent.isRepresent()).isFalse();
            assertThat(oldRepresent.getId()).isEqualTo(representImage.getId());
        }

        @Test
        void 유효하지_않은_경로_변수일_시_400_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            testDataFactory.setAuthentication(tester);

            ProfileImage profileImage = testDataFactory.createProfileImage(tester, 1, false);
            ProfileImage representImage = testDataFactory.createProfileImage(tester, 2, true);

            mockMvc.perform(patch("/api/members/me/profile-images/{imageId}", "profileImage"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 비로그인_시_401_반환() throws Exception {
            Member tester = testDataFactory.createTester();

            ProfileImage profileImage = testDataFactory.createProfileImage(tester, 1, false);
            ProfileImage representImage = testDataFactory.createProfileImage(tester, 2, true);

            mockMvc.perform(patch("/api/members/me/profile-images/{imageId}", profileImage.getId()))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void 존재하지_않는_프로필_이미지일_시_404_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            testDataFactory.setAuthentication(tester);

            ProfileImage profileImage = testDataFactory.createProfileImage(tester, 1, false);
            ProfileImage representImage = testDataFactory.createProfileImage(tester, 2, true);

            mockMvc.perform(patch("/api/members/me/profile-images/{imageId}", 43214321L))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

}
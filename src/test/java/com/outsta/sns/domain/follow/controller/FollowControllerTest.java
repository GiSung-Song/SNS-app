package com.outsta.sns.domain.follow.controller;

import com.outsta.sns.config.support.ControllerTestSupport;
import com.outsta.sns.domain.enums.Visibility;
import com.outsta.sns.domain.follow.entity.Follow;
import com.outsta.sns.domain.follow.repository.FollowRepository;
import com.outsta.sns.domain.member.entity.Member;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FollowControllerTest extends ControllerTestSupport {

    @Autowired
    private FollowRepository followRepository;

    @Nested
    class 팔로우_추가_API_테스트 {

        @Test
        void 팔로우_추가_정상() throws Exception {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();

            testDataFactory.setAuthentication(tester);

            mockMvc.perform(post("/api/members/{memberId}/follow", faker.getId()))
                    .andDo(print())
                    .andExpect(status().isCreated());

            Follow follow = followRepository
                    .findByLoginIdAndMemberId(tester.getId(), faker.getId())
                    .orElseThrow();

            assertThat(follow.getFollower().getId()).isEqualTo(tester.getId());
            assertThat(follow.getFollowing().getId()).isEqualTo(faker.getId());
        }

        @Test
        void 자기_자신_팔로우_요청_시_400_반환() throws Exception {
            Member tester = testDataFactory.createTester();

            testDataFactory.setAuthentication(tester);

            mockMvc.perform(post("/api/members/{memberId}/follow", tester.getId()))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 비로그인_시_401_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();

            mockMvc.perform(post("/api/members/{memberId}/follow", faker.getId()))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void 차단했거나_차단당했을_시_403_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();

            testDataFactory.createBlock(faker, tester);

            testDataFactory.setAuthentication(tester);

            mockMvc.perform(post("/api/members/{memberId}/follow", faker.getId()))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        void 존재하지_않는_회원일_시_404_반환() throws Exception {
            Member tester = testDataFactory.createTester();

            testDataFactory.setAuthentication(tester);

            mockMvc.perform(post("/api/members/{memberId}/follow", 43214321L))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        void 이미_팔로우되어_있을_시_409_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();

            testDataFactory.createFollow(tester, faker);

            testDataFactory.setAuthentication(tester);

            mockMvc.perform(post("/api/members/{memberId}/follow", faker.getId()))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    class 팔로우_취소_API_테스트 {

        @Test
        void 팔로우_취소_정상() throws Exception {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();

            testDataFactory.createFollow(tester, faker);

            testDataFactory.setAuthentication(tester);

            mockMvc.perform(delete("/api/members/{memberId}/follow", faker.getId()))
                    .andDo(print())
                    .andExpect(status().isOk());

            Follow follow = followRepository
                    .findByLoginIdAndMemberId(tester.getId(), faker.getId())
                    .orElse(null);

            assertThat(follow).isNull();
        }

        @Test
        void 자기_자신_팔로우_취소_시도_시_400_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            testDataFactory.setAuthentication(tester);

            mockMvc.perform(delete("/api/members/{memberId}/follow", tester.getId()))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 팔로우하지_않은_경우_400_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();

            testDataFactory.setAuthentication(tester);

            mockMvc.perform(delete("/api/members/{memberId}/follow", faker.getId()))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 비로그인_시_401_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();

            testDataFactory.createFollow(tester, faker);

            mockMvc.perform(delete("/api/members/{memberId}/follow", faker.getId()))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class 팔로워_목록_조회_API_테스트 {

        @Test
        void 내_팔로워_목록_정상_조회() throws Exception {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createFollow(faker, tester);
            testDataFactory.createFollow(dancer, tester);

            testDataFactory.setAuthentication(tester);

            mockMvc.perform(get("/api/members/{memberId}/follower", tester.getId()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.followerList[*].memberId", containsInAnyOrder(dancer.getId().intValue(), faker.getId().intValue())))
                    .andExpect(jsonPath("$.data.followerList[*].nickname", containsInAnyOrder(dancer.getNickname(), faker.getNickname())));
        }

        @Test
        void 회원_팔로워_목록_정상_조회() throws Exception {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createFollow(faker, tester);
            testDataFactory.createFollow(dancer, tester);

            testDataFactory.setAuthentication(dancer);

            mockMvc.perform(get("/api/members/{memberId}/follower", tester.getId()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.followerList[*].memberId", containsInAnyOrder(dancer.getId().intValue(), faker.getId().intValue())))
                    .andExpect(jsonPath("$.data.followerList[*].nickname", containsInAnyOrder(dancer.getNickname(), faker.getNickname())));
        }

        @Test
        void 유효하지_않은_경로_변수일_경우_400_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createFollow(faker, tester);
            testDataFactory.createFollow(dancer, tester);

            testDataFactory.setAuthentication(tester);

            mockMvc.perform(get("/api/members/{memberId}/follower", "tester"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 차단했거나_차단당했을_시_403_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createFollow(faker, tester);
            testDataFactory.createBlock(tester, dancer);

            testDataFactory.setAuthentication(dancer);

            mockMvc.perform(get("/api/members/{memberId}/follower", tester.getId()))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        void 팔로워_전용일_때_팔로워가_아니면_403_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();
            Member dancer = testDataFactory.createDancer();

            tester.updatePrivacy(Visibility.FOLLOWER_ONLY);

            testDataFactory.createFollow(faker, tester);

            testDataFactory.setAuthentication(dancer);

            mockMvc.perform(get("/api/members/{memberId}/follower", tester.getId()))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        void 비공개일_시_403_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();

            tester.updatePrivacy(Visibility.PRIVATE);

            testDataFactory.createFollow(faker, tester);
            testDataFactory.createFollow(tester, faker);

            testDataFactory.setAuthentication(faker);

            mockMvc.perform(get("/api/members/{memberId}/follower", tester.getId()))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        void 존재하지_않는_회원일_시_404_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();

            testDataFactory.createFollow(faker, tester);

            testDataFactory.setAuthentication(faker);

            mockMvc.perform(get("/api/members/{memberId}/follower", 213414L))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class 팔로잉_목록_조회_API_테스트 {

        @Test
        void 내_팔로잉_목록_정상_조회() throws Exception {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createFollow(tester, faker);
            testDataFactory.createFollow(tester, dancer);

            testDataFactory.setAuthentication(tester);

            mockMvc.perform(get("/api/members/{memberId}/following", tester.getId()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.followingList[*].memberId", containsInAnyOrder(dancer.getId().intValue(), faker.getId().intValue())))
                    .andExpect(jsonPath("$.data.followingList[*].nickname", containsInAnyOrder(dancer.getNickname(), faker.getNickname())));
        }

        @Test
        void 회원_팔로잉_목록_정상_조회() throws Exception {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createFollow(tester, faker);
            testDataFactory.createFollow(tester, dancer);

            testDataFactory.setAuthentication(dancer);

            mockMvc.perform(get("/api/members/{memberId}/following", tester.getId()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.followingList[*].memberId", containsInAnyOrder(dancer.getId().intValue(), faker.getId().intValue())))
                    .andExpect(jsonPath("$.data.followingList[*].nickname", containsInAnyOrder(dancer.getNickname(), faker.getNickname())));
        }

        @Test
        void 유효하지_않은_경로_변수일_경우_400_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createFollow(tester, faker);
            testDataFactory.createFollow(tester, dancer);

            testDataFactory.setAuthentication(tester);

            mockMvc.perform(get("/api/members/{memberId}/following", "tester"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 차단했거나_차단당했을_시_403_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createFollow(tester, faker);
            testDataFactory.createBlock(tester, dancer);

            testDataFactory.setAuthentication(dancer);

            mockMvc.perform(get("/api/members/{memberId}/following", tester.getId()))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        void 팔로워_전용일_때_팔로워가_아니면_403_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();
            Member dancer = testDataFactory.createDancer();

            tester.updatePrivacy(Visibility.FOLLOWER_ONLY);

            testDataFactory.createFollow(tester, faker);

            testDataFactory.setAuthentication(dancer);

            mockMvc.perform(get("/api/members/{memberId}/following", tester.getId()))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        void 비공개일_시_403_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();

            tester.updatePrivacy(Visibility.PRIVATE);

            testDataFactory.createFollow(tester, faker);
            testDataFactory.createFollow(faker, tester);

            testDataFactory.setAuthentication(faker);

            mockMvc.perform(get("/api/members/{memberId}/following", tester.getId()))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        void 존재하지_않는_회원일_시_404_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();

            testDataFactory.createFollow(tester, faker);

            testDataFactory.setAuthentication(faker);

            mockMvc.perform(get("/api/members/{memberId}/following", 213414L))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

}
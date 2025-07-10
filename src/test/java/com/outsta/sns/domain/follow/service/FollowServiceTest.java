package com.outsta.sns.domain.follow.service;

import com.outsta.sns.common.error.CustomException;
import com.outsta.sns.common.error.ErrorCode;
import com.outsta.sns.config.support.ServiceTestSupport;
import com.outsta.sns.domain.enums.Visibility;
import com.outsta.sns.domain.follow.dto.FollowerListResponse;
import com.outsta.sns.domain.follow.dto.FollowingListResponse;
import com.outsta.sns.domain.follow.entity.Follow;
import com.outsta.sns.domain.follow.repository.FollowRepository;
import com.outsta.sns.domain.member.entity.Member;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FollowServiceTest extends ServiceTestSupport {

    @Autowired
    private FollowService followService;

    @Autowired
    private FollowRepository followRepository;

    @Nested
    class 팔로우_테스트 {

        @Test
        void 팔로우_정상() {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();

            followService.follow(tester.getId(), faker.getId());

            Follow follow = followRepository
                    .findByLoginIdAndMemberId(tester.getId(), faker.getId())
                    .orElseThrow();

            assertThat(follow.getFollower().getId()).isEqualTo(tester.getId());
            assertThat(follow.getFollowing().getId()).isEqualTo(faker.getId());
        }

        @Test
        void 자기_자신_팔로우_시도_시_400_반환() {
            Member tester = testDataFactory.createTester();

            assertThatThrownBy(() -> followService.follow(tester.getId(), tester.getId()))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    });
        }

        @Test
        void 어느_한쪽이_차단한_경우_403_반환() {
            Member tester = testDataFactory.createTester();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createBlock(dancer, tester);

            assertThatThrownBy(() -> followService.follow(tester.getId(), dancer.getId()))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BLOCK_MEMBER);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    });
        }

        @Test
        void 이미_팔로우한_경우_409_반환() {
            Member tester = testDataFactory.createTester();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createFollow(tester, dancer);

            assertThatThrownBy(() -> followService.follow(tester.getId(), dancer.getId()))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_FOLLOW);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
                    });
        }

        @Test
        void 없는_회원일_경우_404_반환() {
            Member tester = testDataFactory.createTester();

            assertThatThrownBy(() -> followService.follow(tester.getId(), 34214321L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_MEMBER);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

    @Nested
    class 팔로우_취소_테스트 {

        @Test
        void 팔로우_정상_취소() {
            Member tester = testDataFactory.createTester();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createFollow(tester, dancer);

            followService.cancelFollow(tester.getId(), dancer.getId());

            Follow follow = followRepository
                    .findByLoginIdAndMemberId(tester.getId(), dancer.getId())
                    .orElse(null);

            assertThat(follow).isNull();
            ;
        }

        @Test
        void 자기_자신_팔로우_취소_시도_시_400_반환() {
            Member tester = testDataFactory.createTester();

            assertThatThrownBy(() -> followService.cancelFollow(tester.getId(), tester.getId()))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    });
        }

        @Test
        void 팔로우_하지_않았는데_취소_시도_시_400_반환() {
            Member tester = testDataFactory.createTester();
            Member dancer = testDataFactory.createDancer();

            assertThatThrownBy(() -> followService.cancelFollow(tester.getId(), dancer.getId()))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    });
        }
    }

    @Nested
    class 팔로워_목록_조회_테스트 {

        @Test
        void 자신의_팔로워_목록_정상_조회() {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createFollow(faker, tester);
            testDataFactory.createFollow(dancer, tester);

            FollowerListResponse myFollowerList = followService.getMyFollowerList(tester.getId());

            assertThat(myFollowerList.followerList().size()).isEqualTo(2);
            assertThat(myFollowerList.followerList())
                    .containsAnyElementsOf(
                            List.of(
                                    new FollowerListResponse.FollowerMemberDto(faker.getId(), faker.getNickname()),
                                    new FollowerListResponse.FollowerMemberDto(faker.getId(), faker.getNickname())
                            )
                    );
        }

        @Test
        void 회원의_팔로워_목록_정상_조회() {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createFollow(faker, tester);
            testDataFactory.createFollow(dancer, tester);

            FollowerListResponse followerList = followService.getFollowerList(dancer.getId(), tester.getId());

            assertThat(followerList.followerList().size()).isEqualTo(2);
            assertThat(followerList.followerList())
                    .containsAnyElementsOf(
                            List.of(
                                    new FollowerListResponse.FollowerMemberDto(faker.getId(), faker.getNickname()),
                                    new FollowerListResponse.FollowerMemberDto(faker.getId(), faker.getNickname())
                            )
                    );
        }

        @Test
        void 회원의_정보_공개_범위가_PRIVATE인_경우_403_반환() {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createFollow(faker, tester);
            testDataFactory.createFollow(dancer, tester);

            tester.updatePrivacy(Visibility.PRIVATE);

            assertThatThrownBy(() -> followService.getFollowerList(faker.getId(), tester.getId()))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.VISIBILITY_PRIVATE);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    });
        }

        @Test
        void 회원의_정보_공개_범위가_FOLLOWER_ONLY인_경우_팔로우_하지_않았을_떄_403_반환() {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createFollow(dancer, tester);

            tester.updatePrivacy(Visibility.FOLLOWER_ONLY);

            assertThatThrownBy(() -> followService.getFollowerList(faker.getId(), tester.getId()))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.VISIBILITY_FOLLOWER_ONLY);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    });
        }

        @Test
        void 회원_혹은_자신이_차단한_경우_403_반환() {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createFollow(dancer, tester);
            testDataFactory.createBlock(faker, tester);

            assertThatThrownBy(() -> followService.getFollowerList(faker.getId(), tester.getId()))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BLOCK_MEMBER);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    });
        }
    }


    @Nested
    class 팔로잉_목록_조회_테스트 {

        @Test
        void 자신의_팔로잉_목록_정상_조회() {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createFollow(tester, faker);
            testDataFactory.createFollow(tester, dancer);

            FollowingListResponse myFollowingList = followService.getMyFollowingList(tester.getId());

            assertThat(myFollowingList.followingList().size()).isEqualTo(2);
            assertThat(myFollowingList.followingList())
                    .containsAnyElementsOf(
                            List.of(
                                    new FollowingListResponse.FollowingMemberDto(faker.getId(), faker.getNickname()),
                                    new FollowingListResponse.FollowingMemberDto(faker.getId(), faker.getNickname())
                            )
                    );
        }

        @Test
        void 회원의_팔로잉_목록_정상_조회() {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createFollow(tester, faker);
            testDataFactory.createFollow(tester, dancer);

            FollowingListResponse followingList = followService.getFollowingList(dancer.getId(), tester.getId());

            assertThat(followingList.followingList().size()).isEqualTo(2);
            assertThat(followingList.followingList())
                    .containsAnyElementsOf(
                            List.of(
                                    new FollowingListResponse.FollowingMemberDto(faker.getId(), faker.getNickname()),
                                    new FollowingListResponse.FollowingMemberDto(faker.getId(), faker.getNickname())
                            )
                    );
        }

        @Test
        void 회원의_정보_공개_범위가_PRIVATE인_경우_403_반환() {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createFollow(tester, faker);
            testDataFactory.createFollow(tester, dancer);

            tester.updatePrivacy(Visibility.PRIVATE);

            assertThatThrownBy(() -> followService.getFollowingList(faker.getId(), tester.getId()))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.VISIBILITY_PRIVATE);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    });
        }

        @Test
        void 회원의_정보_공개_범위가_FOLLOWER_ONLY인_경우_팔로우_하지_않았을_떄_403_반환() {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createFollow(tester, dancer);

            tester.updatePrivacy(Visibility.FOLLOWER_ONLY);

            assertThatThrownBy(() -> followService.getFollowingList(faker.getId(), tester.getId()))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.VISIBILITY_FOLLOWER_ONLY);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    });
        }

        @Test
        void 회원_혹은_자신이_차단한_경우_403_반환() {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createFollow(tester, dancer);
            testDataFactory.createBlock(tester, faker);

            assertThatThrownBy(() -> followService.getFollowingList(faker.getId(), tester.getId()))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BLOCK_MEMBER);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    });
        }
    }
}
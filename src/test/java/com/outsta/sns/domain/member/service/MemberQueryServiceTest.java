package com.outsta.sns.domain.member.service;

import com.outsta.sns.common.error.CustomException;
import com.outsta.sns.common.error.ErrorCode;
import com.outsta.sns.config.support.ServiceTestSupport;
import com.outsta.sns.domain.enums.Visibility;
import com.outsta.sns.domain.member.dto.response.MemberInfoResponse;
import com.outsta.sns.domain.member.entity.Member;
import com.outsta.sns.domain.profile.entity.ProfileImage;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberQueryServiceTest extends ServiceTestSupport {

    @Autowired
    private MemberQueryService memberQueryService;

    @Nested
    class 회원_상세_조회_테스트 {

        @Test
        void 내_정보_상세_조회_정상() {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createFollow(faker, tester);
            testDataFactory.createFollow(tester, faker);
            testDataFactory.createFollow(dancer, tester);

            ProfileImage profileImage = testDataFactory.createProfileImage(tester, 1, true);

            MemberInfoResponse myInfo = memberQueryService.getMyInfo(tester.getId());

            assertThat(myInfo.followerCount()).isEqualTo(2);
            assertThat(myInfo.followingCount()).isEqualTo(1);
            assertThat(myInfo.profileImageId()).isEqualTo(profileImage.getId());
            assertThat(myInfo.nickname()).isEqualTo(tester.getNickname());
        }

        @Test
        void 회원_정보_상세_조회_전체공개_정상() {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createFollow(faker, tester);
            testDataFactory.createFollow(tester, faker);

            ProfileImage profileImage = testDataFactory.createProfileImage(tester, 1, false);

            MemberInfoResponse memberInfo = memberQueryService.getMemberInfo(dancer.getId(), tester.getId());

            assertThat(memberInfo.followerCount()).isEqualTo(1);
            assertThat(memberInfo.followingCount()).isEqualTo(1);
            assertThat(memberInfo.profileImageId()).isEqualTo(profileImage.getId());
            assertThat(memberInfo.nickname()).isEqualTo(tester.getNickname());
        }

        @Test
        void 회원이_없는_경우_400_반환() {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createFollow(faker, tester);
            testDataFactory.createFollow(tester, faker);
            testDataFactory.createFollow(dancer, tester);

            assertThatThrownBy(() -> memberQueryService.getMemberInfo(dancer.getId(), 4321432L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_MEMBER);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }

        @Test
        void 차단했거나_차단_당했을_경우_403_반환() {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createFollow(faker, tester);
            testDataFactory.createFollow(tester, faker);

            testDataFactory.createBlock(tester, dancer);

            assertThatThrownBy(() -> memberQueryService.getMemberInfo(dancer.getId(), tester.getId()))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BLOCK_MEMBER);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    });
        }

        @Test
        void 팔로워_전용인데_팔로워가_아닌_경우_403_반환() {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createFollow(faker, tester);
            testDataFactory.createFollow(tester, faker);

            tester.updatePrivacy(Visibility.FOLLOWER_ONLY);

            assertThatThrownBy(() -> memberQueryService.getMemberInfo(dancer.getId(), tester.getId()))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.VISIBILITY_FOLLOWER_ONLY);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    });
        }

        @Test
        void 팔로워_전용인데_팔로워인_경우_정상() {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createFollow(faker, tester);
            testDataFactory.createFollow(tester, faker);
            testDataFactory.createFollow(dancer, tester);

            tester.updatePrivacy(Visibility.FOLLOWER_ONLY);

            ProfileImage profileImage = testDataFactory.createProfileImage(tester, 1, false);

            MemberInfoResponse memberInfo = memberQueryService.getMemberInfo(dancer.getId(), tester.getId());

            assertThat(memberInfo.followerCount()).isEqualTo(2);
            assertThat(memberInfo.followingCount()).isEqualTo(1);
            assertThat(memberInfo.profileImageId()).isEqualTo(profileImage.getId());
            assertThat(memberInfo.nickname()).isEqualTo(tester.getNickname());
        }

        @Test
        void 비공개인_경우_403_반환() {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();

            testDataFactory.createFollow(faker, tester);
            testDataFactory.createFollow(tester, faker);

            tester.updatePrivacy(Visibility.PRIVATE);

            assertThatThrownBy(() -> memberQueryService.getMemberInfo(faker.getId(), tester.getId()))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.VISIBILITY_PRIVATE);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    });
        }
    }
}
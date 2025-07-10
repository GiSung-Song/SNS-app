package com.outsta.sns.domain.block.service;

import com.outsta.sns.common.error.CustomException;
import com.outsta.sns.common.error.ErrorCode;
import com.outsta.sns.config.support.ServiceTestSupport;
import com.outsta.sns.domain.block.dto.BlockListResponse;
import com.outsta.sns.domain.block.entity.Block;
import com.outsta.sns.domain.block.repository.BlockRepository;
import com.outsta.sns.domain.member.entity.Member;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlockServiceTest extends ServiceTestSupport {

    @Autowired
    private BlockService blockService;

    @Autowired
    private BlockRepository blockRepository;

    @Nested
    class 회원_차단_테스트 {

        @Test
        void 회원_정상_차단() {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();

            blockService.blockMember(tester.getId(), faker.getId());

            Block block = blockRepository.findByLoginIdAndMemberId(tester.getId(), faker.getId())
                    .orElseThrow();

            assertThat(block.getBlocker().getId()).isEqualTo(tester.getId());
            assertThat(block.getBlocked().getId()).isEqualTo(faker.getId());
        }

        @Test
        void 자기_자신_차단_시도_시_400_반환() {
            Member tester = testDataFactory.createTester();

            assertThatThrownBy(() -> blockService.blockMember(tester.getId(), tester.getId()))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    });
        }

        @Test
        void 없는_회원_차단_시도_시_404_반환() {
            Member tester = testDataFactory.createTester();

            assertThatThrownBy(() -> blockService.blockMember(tester.getId(), 43214321L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_MEMBER);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }

        @Test
        void 이미_차단한_회원_차단_시도_시_409_반환() {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();

            Block block = Block.builder()
                    .blocked(faker)
                    .blocker(tester)
                    .build();

            blockRepository.save(block);

            assertThatThrownBy(() -> blockService.blockMember(tester.getId(), faker.getId()))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_BLOCKED);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
                    });
        }
    }

    @Nested
    class 차단_취소_테스트 {

        @Test
        void 차단_취소_정상() {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();

            testDataFactory.createBlock(tester, faker);

            blockService.cancelBlock(tester.getId(), faker.getId());

            Block block = blockRepository
                    .findByLoginIdAndMemberId(tester.getId(), faker.getId())
                    .orElse(null);

            assertThat(block).isNull();
        }

        @Test
        void 자기_자신_차단_취소_시도_시_400_반환() {
            Member tester = testDataFactory.createTester();

            assertThatThrownBy(() -> blockService.cancelBlock(tester.getId(), tester.getId()))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    });
        }

        @Test
        void 차단_되지_않은_회원_시도_시_400_반환() {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();

            assertThatThrownBy(() -> blockService.cancelBlock(tester.getId(), faker.getId()))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    });
        }
    }

    @Nested
    class 차단_목록_조회_테스트 {

        @Test
        void 차단_목록_조회() {
            Member tester = testDataFactory.createTester();
            Member faker = testDataFactory.createFaker();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createBlock(tester, faker);
            testDataFactory.createBlock(tester, dancer);

            BlockListResponse blockList = blockService.getBlockList(tester.getId());

            assertThat(blockList.blockedList().size()).isEqualTo(2);
            assertThat(blockList.blockedList())
                    .containsAnyElementsOf(
                            List.of(
                                    new BlockListResponse.BlockMemberDto(faker.getId(), faker.getNickname()),
                                    new BlockListResponse.BlockMemberDto(dancer.getId(), dancer.getNickname())
                            )
                    );
        }
    }
}
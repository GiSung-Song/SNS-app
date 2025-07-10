package com.outsta.sns.domain.block.controller;

import com.outsta.sns.config.support.ControllerTestSupport;
import com.outsta.sns.domain.block.entity.Block;
import com.outsta.sns.domain.block.repository.BlockRepository;
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

class BlockControllerTest extends ControllerTestSupport {

    @Autowired
    private BlockRepository blockRepository;

    @Nested
    class 회원_차단_API_테스트 {

        @Test
        void 회원_차단_정상() throws Exception {
            Member tester = testDataFactory.createTester();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.setAuthentication(tester);

            mockMvc.perform(post("/api/block/{memberId}", dancer.getId()))
                    .andExpect(status().isOk())
                    .andDo(print());

            Block block = blockRepository
                    .findByLoginIdAndMemberId(tester.getId(), dancer.getId())
                    .orElseThrow();

            assertThat(block.getBlocker().getId()).isEqualTo(tester.getId());
            assertThat(block.getBlocked().getId()).isEqualTo(dancer.getId());
        }

        @Test
        void 자기_자신_차단_시도_시_400_반환() throws Exception {
            Member tester = testDataFactory.createTester();

            testDataFactory.setAuthentication(tester);

            mockMvc.perform(post("/api/block/{memberId}", tester.getId()))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        void 비로그인_시_401_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            Member dancer = testDataFactory.createDancer();

            mockMvc.perform(post("/api/block/{memberId}", dancer.getId()))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        void 존재하지_않는_회원일_시_404_반환() throws Exception {
            Member tester = testDataFactory.createTester();

            testDataFactory.setAuthentication(tester);

            mockMvc.perform(post("/api/block/{memberId}", 231421432L))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }

        @Test
        void 이미_차단한_회원일_시_409_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createBlock(tester, dancer);

            testDataFactory.setAuthentication(tester);

            mockMvc.perform(post("/api/block/{memberId}", dancer.getId()))
                    .andExpect(status().isConflict())
                    .andDo(print());
        }
    }

    @Nested
    class 차단_취소_API_테스트 {

        @Test
        void 차단_취소_정상() throws Exception {
            Member tester = testDataFactory.createTester();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createBlock(tester, dancer);

            testDataFactory.setAuthentication(tester);

            mockMvc.perform(delete("/api/block/{memberId}", dancer.getId()))
                    .andExpect(status().isOk())
                    .andDo(print());

            Block block = blockRepository
                    .findByLoginIdAndMemberId(tester.getId(), dancer.getId())
                    .orElse(null);

            assertThat(block).isNull();
        }

        @Test
        void 자기_자신_차단_취소_시_400_반환() throws Exception {
            Member tester = testDataFactory.createTester();

            testDataFactory.setAuthentication(tester);

            mockMvc.perform(delete("/api/block/{memberId}", tester.getId()))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        void 차단하지_않은_경우_400_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.setAuthentication(tester);

            mockMvc.perform(delete("/api/block/{memberId}", dancer.getId()))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        void 비로그인_시_401_반환() throws Exception {
            Member tester = testDataFactory.createTester();
            Member dancer = testDataFactory.createDancer();

            testDataFactory.createBlock(tester, dancer);

            mockMvc.perform(delete("/api/block/{memberId}", dancer.getId()))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }

    @Nested
    class 차단_목록_조회_API_테스트 {

        @Test
        void 차단_목록_조회_성공() throws Exception {
            Member tester = testDataFactory.createTester();
            Member dancer = testDataFactory.createDancer();
            Member faker = testDataFactory.createFaker();

            testDataFactory.setAuthentication(tester);

            testDataFactory.createBlock(tester, faker);
            testDataFactory.createBlock(tester, dancer);

            mockMvc.perform(get("/api/block"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.blockedList[*].memberId", containsInAnyOrder(dancer.getId().intValue(), faker.getId().intValue())))
                    .andExpect(jsonPath("$.data.blockedList[*].nickname", containsInAnyOrder(dancer.getNickname(), faker.getNickname())));
        }
    }
}
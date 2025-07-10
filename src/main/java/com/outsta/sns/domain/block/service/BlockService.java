package com.outsta.sns.domain.block.service;

import com.outsta.sns.common.error.CustomException;
import com.outsta.sns.common.error.ErrorCode;
import com.outsta.sns.domain.block.dto.BlockListResponse;
import com.outsta.sns.domain.block.entity.Block;
import com.outsta.sns.domain.block.repository.BlockQueryRepository;
import com.outsta.sns.domain.block.repository.BlockRepository;
import com.outsta.sns.domain.member.entity.Member;
import com.outsta.sns.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 차단 관련 서비스
 *
 * <p>차단 기능, 차단 해제 기능, 차단 목록 조회</p>
 */
@Service
@RequiredArgsConstructor
public class BlockService {

    private final BlockRepository blockRepository;
    private final BlockQueryRepository blockQueryRepository;
    private final MemberService memberService;
    private final BlockFollowRelationService blockFollowRelationService;

    /**
     * 회원 차단
     * - 팔로우 되어있으면 팔로우 삭제
     *
     * @param loginId  현재 로그인한 회원의 식별자 ID
     * @param memberId 차단하려는 회원의 식별자 ID
     * @throws CustomException 회원이 없거나, 자기 자신을 차단하려고 하는 경우, 이미 차단한 경우에 발생
     */
    @Transactional
    public void blockMember(Long loginId, Long memberId) {
        if (loginId.equals(memberId)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        if (blockQueryRepository.existsByLoginIdAndMemberId(loginId, memberId)) {
            throw new CustomException(ErrorCode.DUPLICATE_BLOCKED);
        }

        Member blocker = memberService.findActiveMemberById(loginId);
        Member blocked = memberService.findActiveMemberById(memberId);

        blockFollowRelationService.deleteFollowRelation(loginId, memberId);

        Block block = Block.builder()
                .blocker(blocker)
                .blocked(blocked)
                .build();

        blockRepository.save(block);
    }

    /**
     * 회원 차단 취소
     * @param loginId  현재 로그인한 회원의 식별자 ID
     * @param memberId 차단 취소 하려는 회원의 식별자 ID
     * @throws CustomException 회원이 없거나, 자기 자신을 차단 취소하려고 하는 경우, 차단하지 않은 경우에 발생
     */
    @Transactional
    public void cancelBlock(Long loginId, Long memberId) {
        if (loginId.equals(memberId)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        Block block = blockRepository.findByLoginIdAndMemberId(loginId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));

        blockRepository.delete(block);
    }

    /**
     * 차단 목록 조회
     * @param loginId 현재 로그인한 회원의 식별자 ID
     * @return BlockListResponse 차단 목록 (회원 식별자 ID, 닉네임)
     */
    @Transactional(readOnly = true)
    public BlockListResponse getBlockList(Long loginId) {
        List<BlockListResponse.BlockMemberDto> blockedMemberList =
                blockQueryRepository.getBlockedMemberList(loginId);

        return new BlockListResponse(blockedMemberList);
    }
}

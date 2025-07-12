package com.outsta.sns.domain.follow.service;

import com.outsta.sns.common.error.CustomException;
import com.outsta.sns.common.error.ErrorCode;
import com.outsta.sns.domain.block.service.BlockFollowRelationService;
import com.outsta.sns.domain.follow.dto.FollowerCountDto;
import com.outsta.sns.domain.follow.dto.FollowerListResponse;
import com.outsta.sns.domain.follow.dto.FollowingCountDto;
import com.outsta.sns.domain.follow.dto.FollowingListResponse;
import com.outsta.sns.domain.follow.entity.Follow;
import com.outsta.sns.domain.follow.repository.FollowQueryRepository;
import com.outsta.sns.domain.follow.repository.FollowRepository;
import com.outsta.sns.domain.member.access.AccessPolicy;
import com.outsta.sns.domain.member.dto.response.util.MemberAccessCheckDto;
import com.outsta.sns.domain.member.entity.Member;
import com.outsta.sns.domain.member.service.MemberUtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 팔로우 관련 서비스
 *
 * <p>팔로우, 팔로우 취소, 팔로워 목록 조회, 팔로잉 목록 조회</p>
 */
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final FollowQueryRepository followQueryRepository;
    private final BlockFollowRelationService blockFollowRelationService;
    private final MemberUtilService memberUtilService;
    private final AccessPolicy accessPolicy;

    /**
     * 팔로우
     * - 어느 한쪽이라도 차단되어 있으면 팔로우 불가능
     *
     * @param loginId  현재 로그인한 회원의 식별자 ID
     * @param memberId 팔로우하려는 회원의 식별자 ID
     * @throws CustomException 회원이 없거나, 자기 자신을 팔로우하려고 하는 경우, 차단한 경우, 이미 팔로우한 경우에 발생
     */
    @Caching(evict = {
            @CacheEvict(value = "followingCount", key = "#loginId"),
            @CacheEvict(value = "followerCount", key = "#memberId")
    })
    public void follow(Long loginId, Long memberId) {
        if (loginId.equals(memberId)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        if (blockFollowRelationService.isBlockedWhoever(loginId, memberId)) {
            throw new CustomException(ErrorCode.BLOCK_MEMBER);
        }

        if (followQueryRepository.existsByLoginIdAndMemberId(loginId, memberId)) {
            throw new CustomException(ErrorCode.DUPLICATE_FOLLOW);
        }

        Member follower = memberUtilService.findActiveMemberById(loginId);
        Member following = memberUtilService.findActiveMemberById(memberId);

        Follow follow = Follow.builder()
                .follower(follower)
                .following(following)
                .build();

        followRepository.save(follow);
    }

    /**
     * 팔로우 취소
     *
     * @param loginId  현재 로그인한 회원의 식별자 ID
     * @param memberId 팔로우 취소 하려는 회원의 식별자 ID
     * @throws CustomException 회원이 없거나, 자기 자신을 팔로우 취소하려고 하는 경우, 팔로우하지 않은 경우에 발생
     */
    @Caching(evict = {
            @CacheEvict(value = "followingCount", key = "#loginId"),
            @CacheEvict(value = "followerCount", key = "#memberId")
    })
    public void cancelFollow(Long loginId, Long memberId) {
        if (loginId.equals(memberId)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        Follow follow = followRepository.findByLoginIdAndMemberId(loginId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));

        followRepository.delete(follow);
    }

    /**
     * 내 팔로워 목록 조회
     *
     * @param loginId 현재 로그인한 회원의 식별자 ID
     * @return 내 팔로워 목록 (회원 식별자 ID, 닉네임)
     */
    public FollowerListResponse getMyFollowerList(Long loginId) {
        List<FollowerListResponse.FollowerMemberDto> followerList
                = followQueryRepository.getFollowerList(loginId);

        return new FollowerListResponse(followerList);
    }

    /**
     * 회원의 팔로워 목록 조회
     *
     * @param loginId  현재 로그인한 회원의 식별자 ID
     * @param memberId 조회 하려는 회원의 식별자 ID
     * @return 팔로워 목록 (회원 식별자 ID, 닉네임)
     */
    public FollowerListResponse getFollowerList(Long loginId, Long memberId) {
        MemberAccessCheckDto member = memberUtilService.getActiveMemberFollow(memberId);
        accessPolicy.checkVisibilityAndBlock(loginId, member.id(), member.visibility());

        List<FollowerListResponse.FollowerMemberDto> followerList
                = followQueryRepository.getFollowerList(memberId);

        return new FollowerListResponse(followerList);
    }

    /**
     * 내 팔로잉 목록 조회
     *
     * @param loginId 현재 로그인한 회원의 식별자 ID
     * @return 내 팔로잉 목록 (회원 식별자 ID, 닉네임)
     */
    public FollowingListResponse getMyFollowingList(Long loginId) {
        List<FollowingListResponse.FollowingMemberDto> followingList
                = followQueryRepository.getFollowingList(loginId);

        return new FollowingListResponse(followingList);
    }

    /**
     * 회원의 팔로잉 목록 조회
     *
     * @param loginId  현재 로그인한 회원의 식별자 ID
     * @param memberId 조회 하려는 회원의 식별자 ID
     * @return 팔로잉 목록 (회원 식별자 ID, 닉네임)
     */
    public FollowingListResponse getFollowingList(Long loginId, Long memberId) {
        MemberAccessCheckDto member = memberUtilService.getActiveMemberFollow(memberId);
        accessPolicy.checkVisibilityAndBlock(loginId, member.id(), member.visibility());

        List<FollowingListResponse.FollowingMemberDto> followingList
                = followQueryRepository.getFollowingList(memberId);

        return new FollowingListResponse(followingList);
    }

    /**
     * 회원의 팔로워 수 조회
     *
     * @param memberId 조회 하려는 회원의 식별자 ID
     * @return 회원의 팔로워 수
     */
    @Cacheable(value = "followerCount", key = "#memberId")
    public FollowerCountDto getFollowerCount(Long memberId) {
        return followQueryRepository.getFollowerCount(memberId);
    }

    /**
     * 회원의 팔로잉 수 조회
     *
     * @param memberId 조회 하려는 회원의 식별자 ID
     * @return 회원의 팔로잉 수
     */
    @Cacheable(value = "followingCount", key = "#memberId")
    public FollowingCountDto getFollowingCount(Long memberId) {
        return followQueryRepository.getFollowingCount(memberId);
    }
}
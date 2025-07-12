package com.outsta.sns.domain.follow.dto;

/**
 * 팔로잉 수 DTO
 * - 내부 로직용 DTO
 */
public record FollowingCountDto(
        /** 팔로잉 수 */
        long count
) {
}

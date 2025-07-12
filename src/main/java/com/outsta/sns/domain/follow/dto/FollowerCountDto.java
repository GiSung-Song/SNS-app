package com.outsta.sns.domain.follow.dto;

/**
 * 팔로워 수 DTO
 * - 내부 로직용 DTO
 */
public record FollowerCountDto(
        /** 팔로워 수 */
        long count
) {
}

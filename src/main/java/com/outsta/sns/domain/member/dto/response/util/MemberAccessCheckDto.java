package com.outsta.sns.domain.member.dto.response.util;

import com.outsta.sns.domain.enums.Visibility;
import com.outsta.sns.domain.member.entity.Member;

/**
 * 회원 DTO -> 접근 체크용
 * - 공통 내부 로직용 Util DTO
 */
public record MemberAccessCheckDto(
        /** 회원 식별자 ID */
        Long id,

        /** 회원 정보 공개 범위 */
        Visibility visibility
) {
    public static MemberAccessCheckDto from(Member member) {
        return new MemberAccessCheckDto(
                member.getId(),
                member.getVisibility()
        );
    }
}
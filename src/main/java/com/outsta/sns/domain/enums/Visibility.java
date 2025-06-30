package com.outsta.sns.domain.enums;

import lombok.Getter;

public enum Visibility implements EnumUtil {
    PUBLIC("전체 공개"),
    FOLLOWER_ONLY("팔로워 공개"),
    PRIVATE("비공개")
    ;

    @Getter
    private final String value;

    Visibility(String value) {
        this.value = value;
    }

    @Override
    public String getCode() {
        return name();
    }
}
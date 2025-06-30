package com.outsta.sns.domain.enums;

public enum LikeType implements EnumUtil {
    POST("게시글"),
    REPLY("댓글"),
    ;

    private final String value;

    LikeType(String value) {
        this.value = value;
    }

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getValue() {
        return value;
    }
}
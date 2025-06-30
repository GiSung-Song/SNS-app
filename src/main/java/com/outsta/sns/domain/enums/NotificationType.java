package com.outsta.sns.domain.enums;

public enum NotificationType implements EnumUtil {
    NEW_POST("새 게시글"),
    POST_REPLY("게시글 댓글"),
    POST_LIKE("게시글 좋아요"),
    REPLY_LIKE("댓글 좋아요"),
    FOLLOWER("새 팔로워"),
    REPLY_REPLY("댓글의 댓글")
    ;

    private final String value;

    NotificationType(String value) {
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

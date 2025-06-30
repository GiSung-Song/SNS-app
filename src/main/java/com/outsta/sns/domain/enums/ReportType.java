package com.outsta.sns.domain.enums;

public enum ReportType implements EnumUtil {
    MEMBER("회원"),
    POST("게시글"),
    REPLY("댓글"),
    ;

    private final String value;

    ReportType(String value) {
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

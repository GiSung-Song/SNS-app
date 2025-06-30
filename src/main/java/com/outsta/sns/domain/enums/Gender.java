package com.outsta.sns.domain.enums;

public enum Gender implements EnumUtil {
    MALE("남자"),
    FEMALE("여자"),
    ;

    private final String value;

    Gender(String value) {
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
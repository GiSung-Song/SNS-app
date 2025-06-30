package com.outsta.sns.domain.enums;

public enum Activation implements EnumUtil {
    ACTIVE("활동중"),
    SUSPENDED("정지"),
    DELETED("회원 탈퇴"),
    ;

    private final String value;

    Activation(String value) {
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
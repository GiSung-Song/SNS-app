package com.outsta.sns.domain.enums;

public enum Role  implements EnumUtil {
    GUEST("ROLE_GUEST"),
    MEMBER("ROLE_MEMBER"),
    ADMIN("ROLE_ADMIN")
    ;

    private final String value;

    Role(String value) {
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
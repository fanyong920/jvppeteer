package com.ruiyun.jvppeteer.cdp.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum IPAddressSpace {
    Loopback("Loopback"),
    Local("Local"),
    Public("Public"),
    Unknown("Unknown");

    private final String value;

    IPAddressSpace(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static IPAddressSpace fromValue(String value) {
        for (IPAddressSpace space : IPAddressSpace.values()) {
            if (space.value.equals(value)) {
                return space;
            }
        }
        // 遇到未知的枚举值时，返回 Unknown 而不是抛出异常
        return Unknown;
    }
}

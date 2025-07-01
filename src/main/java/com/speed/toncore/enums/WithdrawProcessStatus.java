package com.speed.toncore.enums;

import lombok.Getter;

@Getter
public enum WithdrawProcessStatus {

    UNPAID("unpaid"),
    PAID("paid"),
    FAILED("failed");

    private final String value;

    WithdrawProcessStatus(String value) {
        this.value = value;
    }
}

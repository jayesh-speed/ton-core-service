package com.speed.toncore.enums;

import lombok.Getter;

@Getter
public enum WithdrawProcessReference {

    WITHDRAW("withdraw"),
    PAYOUT("payout");

    private final String value;

    WithdrawProcessReference(String value) {
        this.value = value;
    }
}

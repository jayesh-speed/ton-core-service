package com.speed.toncore.enums;

import lombok.Getter;

@Getter
public enum OnChainTxStatus{
	PENDING(0),
	CONFIRMED(1);

	private final int value;

	OnChainTxStatus(int value) {
		this.value = value;
	}

}

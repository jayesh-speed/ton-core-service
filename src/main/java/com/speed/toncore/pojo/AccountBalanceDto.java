package com.speed.toncore.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.speed.toncore.constants.JsonKeys;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
public class AccountBalanceDto {

	@JsonProperty(JsonKeys.TonIndexer.OK)
	private boolean ok;

	@JsonProperty(JsonKeys.TonIndexer.RESULT)
	private BigInteger result;
}

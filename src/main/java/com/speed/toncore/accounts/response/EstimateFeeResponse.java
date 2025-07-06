package com.speed.toncore.accounts.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.speed.toncore.constants.JsonKeys;
import lombok.Builder;
import lombok.Getter;

import java.math.BigInteger;

@Getter
@Builder
public class EstimateFeeResponse {

	@JsonProperty(JsonKeys.TRANSACTION_FEE)
	private BigInteger transactionFee;
}

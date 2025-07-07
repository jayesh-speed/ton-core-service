package com.speed.toncore.accounts.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.speed.toncore.constants.JsonKeys;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class EstimateFeeResponse {

	@JsonProperty(JsonKeys.CHAIN_ID)
	private Integer chainId;

	@JsonProperty(JsonKeys.IS_MAIN_NET)
	private boolean mainNet;

	@JsonProperty(JsonKeys.TRANSACTION_FEE)
	private BigDecimal transactionFee;
}

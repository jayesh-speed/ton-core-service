package com.speed.toncore.sweep.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.speed.toncore.constants.JsonKeys;
import lombok.Builder;

@Builder
public class SweepResponse {

	@JsonProperty(JsonKeys.TRANSACTION_HASH)
	private String transactionHash;
}

package com.speed.toncore.accounts.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.speed.toncore.constants.JsonKeys;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeployedAccountResponse {

	@JsonProperty(JsonKeys.TRANSACTION_HASH)
	private String transactionHash;
}

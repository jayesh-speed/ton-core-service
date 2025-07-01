package com.speed.toncore.accounts.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.speed.toncore.constants.JsonKeys;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TonAccountResponse {

	@JsonProperty(JsonKeys.ADDRESS)
	private String address;

	@JsonProperty(JsonKeys.PUBLIC_KEY)
	private String publicKey;

	@JsonProperty(JsonKeys.LOCAL_BALANCE)
	private String localBalance;
}

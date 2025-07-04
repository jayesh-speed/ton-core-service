package com.speed.toncore.accounts.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.speed.toncore.constants.JsonKeys;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class BalanceResponse {

	@JsonProperty(JsonKeys.BALANCE)
	private BigDecimal balance;
}

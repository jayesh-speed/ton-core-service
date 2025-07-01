package com.speed.toncore.accounts.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.speed.javacommon.util.serializer.BigDecimalSerializer;
import com.speed.toncore.constants.JsonKeys;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class TonAccountResponse {

	@JsonProperty(JsonKeys.ADDRESS)
	private String address;

	@JsonProperty(JsonKeys.LOCAL_BALANCE)
	@JsonSerialize(using = BigDecimalSerializer.class)
	private BigDecimal localBalance;
}

package com.speed.toncore.withdraw.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.speed.toncore.constants.JsonKeys;
import com.speed.toncore.constants.ValidationMessages;
import com.speed.toncore.util.TonRawAddressDeserializer;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WithdrawRequest {

	@JsonProperty(JsonKeys.VALUE)
	@NotNull(message = JsonKeys.VALUE + ValidationMessages.NOTNULL)
	private String value;

	@JsonProperty(JsonKeys.TO_ADDRESS)
	@NotNull(message = JsonKeys.TO_ADDRESS + ValidationMessages.NOTNULL)
	@JsonDeserialize(using = TonRawAddressDeserializer.class)
	private String toAddress;

	@JsonProperty(JsonKeys.FROM_ADDRESS)
	@JsonDeserialize(using = TonRawAddressDeserializer.class)
	private String fromAddress;

	@JsonProperty(JsonKeys.JETTON_ADDRESS)
	@JsonDeserialize(using = TonRawAddressDeserializer.class)
	private String jettonAddress;

	@JsonProperty(JsonKeys.JETTON_SYMBOL)
	private String jettonSymbol;
}

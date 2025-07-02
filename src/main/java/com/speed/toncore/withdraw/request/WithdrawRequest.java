package com.speed.toncore.withdraw.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.speed.toncore.constants.JsonKeys;
import com.speed.toncore.constants.ValidationMessages;
import com.speed.toncore.util.TonAddressDeserializer;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WithdrawRequest {

	@JsonProperty(JsonKeys.VALUE)
	@NotNull(message = JsonKeys.VALUE + ValidationMessages.NOT_NULL)
	private String value;

	@JsonProperty(JsonKeys.TO_ADDRESS)
	@NotNull(message = JsonKeys.TO_ADDRESS + ValidationMessages.NOT_NULL)
	@JsonDeserialize(using = TonAddressDeserializer.class)
	private String toAddress;

	@JsonProperty(JsonKeys.FROM_ADDRESS)
	@JsonDeserialize(using = TonAddressDeserializer.class)
	private String fromAddress;

	@JsonProperty(JsonKeys.JETTON_MASTER_ADDRESS)
	@JsonDeserialize(using = TonAddressDeserializer.class)
	private String jettonMasterAddress;

	@JsonProperty(JsonKeys.JETTON_SYMBOL)
	private String jettonSymbol;
}

package com.speed.toncore.sweep.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.constants.JsonKeys;
import com.speed.toncore.constants.ValidationMessages;
import com.speed.toncore.util.TonAddressDeserializer;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SweepRequest {

	@JsonProperty(JsonKeys.TOKEN_ADDRESS)
	@NotBlank(message = JsonKeys.TOKEN_ADDRESS + ValidationMessages.NOT_BLANK)
	@JsonDeserialize(using = TonAddressDeserializer.class)
	private String tokenAddress;

	@JsonProperty(JsonKeys.ADDRESS)
	@NotBlank(message = Errors.BLANK_ADDRESS)
	@JsonDeserialize(using = TonAddressDeserializer.class)
	private String fromAddress;
}

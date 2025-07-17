package com.speed.toncore.accounts.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.speed.toncore.constants.JsonKeys;
import com.speed.toncore.constants.ValidationMessages;
import com.speed.toncore.util.TonAddressDeserializer;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TonMainAccountRequest {

	@JsonProperty(JsonKeys.TOKEN_ADDRESS)
	@NotBlank(message = JsonKeys.TOKEN_ADDRESS + ValidationMessages.NOT_BLANK)
	@JsonDeserialize(using = TonAddressDeserializer.class)
	private String tokenAddress;
}

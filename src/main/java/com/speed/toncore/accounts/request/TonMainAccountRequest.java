package com.speed.toncore.accounts.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.constants.JsonKeys;
import com.speed.toncore.util.TonRawAddressDeserializer;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TonMainAccountRequest {
	@JsonProperty(JsonKeys.JETTON_MASTER_ADDRESS)
	@NotBlank(message = Errors.BLANK_JETTON_MASTER_ADDRESS)
	@JsonDeserialize(using = TonRawAddressDeserializer.class)
	private String jettonMasterAddress;
}

package com.speed.toncore.sweep.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.constants.JsonKeys;
import com.speed.toncore.util.TonAddressDeserializer;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SweepRequest {

	@JsonProperty(JsonKeys.JETTON_MASTER_ADDRESS)
	@NotBlank(message = Errors.BLANK_JETTON_MASTER_ADDRESS)
	@JsonDeserialize(using = TonAddressDeserializer.class)
	private String jettonMasterAddress;

	@JsonProperty(JsonKeys.ADDRESS)
	@NotBlank(message = Errors.BLANK_ADDRESS)
	@JsonDeserialize(using = TonAddressDeserializer.class)
	private String fromAddress;
}

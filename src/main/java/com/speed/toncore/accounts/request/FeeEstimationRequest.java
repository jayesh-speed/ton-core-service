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
public class FeeEstimationRequest {

	@JsonProperty(JsonKeys.FROM_ADDRESS)
	@JsonDeserialize(using = TonAddressDeserializer.class)
	private String fromAddress;

	@JsonProperty(JsonKeys.TO_ADDRESS)
	@NotBlank(message = JsonKeys.TO_ADDRESS + ValidationMessages.NOT_BLANK)
	@JsonDeserialize(using = TonAddressDeserializer.class)
	private String toAddress;

	@JsonProperty(JsonKeys.JETTON_MASTER_ADDRESS)
	@NotBlank(message = JsonKeys.JETTON_MASTER_ADDRESS + ValidationMessages.NOT_BLANK)
	@JsonDeserialize(using = TonAddressDeserializer.class)
	private String jettonMasterAddress;
}

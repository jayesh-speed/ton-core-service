package com.speed.toncore.jettons.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.constants.JsonKeys;
import com.speed.toncore.constants.ValidationMessages;
import com.speed.toncore.util.TonAddressDeserializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TonJettonRequest {

	@JsonProperty(JsonKeys.JETTON_MASTER_ADDRESS)
	@JsonDeserialize(using = TonAddressDeserializer.class)
	@NotBlank(message = JsonKeys.JETTON_MASTER_ADDRESS + ValidationMessages.NOT_BLANK)
	private String jettonMasterAddress;

	@JsonProperty(JsonKeys.JETTON_NAME)
	@NotBlank(message = JsonKeys.JETTON_NAME + ValidationMessages.NOT_BLANK)
	private String jettonName;

	@JsonProperty(JsonKeys.JETTON_SYMBOL)
	@NotBlank(message = JsonKeys.JETTON_SYMBOL + ValidationMessages.NOT_BLANK)
	private String jettonSymbol;

	@JsonProperty(JsonKeys.NO_OF_CELL)
	private Integer noOfCell;

	@JsonProperty(JsonKeys.NO_OF_BITS)
	private Integer noOfBits;

	@JsonProperty(JsonKeys.GAS_UNIT)
	private Integer gasUnit;

	@JsonProperty(JsonKeys.DEPLOYMENT_COST)
	private Long deploymentCost;

	@JsonProperty(JsonKeys.RESERVE_STORAGE_FEE)
	private Long reserveStorageFee;

	@JsonProperty(JsonKeys.DECIMALS)
	@NotNull(message = JsonKeys.DECIMALS + ValidationMessages.NOT_NULL)
	@Positive(message = JsonKeys.DECIMALS + Errors.MUST_BE_POSITIVE)
	private Integer decimals;
}

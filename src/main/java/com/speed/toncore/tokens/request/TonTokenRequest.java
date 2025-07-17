package com.speed.toncore.tokens.request;

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
public class TonTokenRequest {

	@JsonProperty(JsonKeys.TOKEN_ADDRESS)
	@JsonDeserialize(using = TonAddressDeserializer.class)
	@NotBlank(message = JsonKeys.TOKEN_ADDRESS + ValidationMessages.NOT_BLANK)
	private String tokenAddress;

	@JsonProperty(JsonKeys.TOKEN_NAME)
	@NotBlank(message = JsonKeys.TOKEN_NAME + ValidationMessages.NOT_BLANK)
	private String tokenName;

	@JsonProperty(JsonKeys.TOKEN_SYMBOL)
	@NotBlank(message = JsonKeys.TOKEN_SYMBOL + ValidationMessages.NOT_BLANK)
	private String tokenSymbol;

	@JsonProperty(JsonKeys.NO_OF_CELL)
	private Integer noOfCell;

	@JsonProperty(JsonKeys.NO_OF_BITS)
	private Integer noOfBits;

	@JsonProperty(JsonKeys.GAS_UNIT)
	private Integer gasUnit;

	@JsonProperty(JsonKeys.DEPLOYMENT_COST)
	private Long deploymentCost;

	@JsonProperty(JsonKeys.NO_OF_CELL_V3)
	private Integer noOfCellV3;

	@JsonProperty(JsonKeys.NO_OF_BITS_V3)
	private Integer noOfBitsV3;

	@JsonProperty(JsonKeys.GAS_UNIT_V3)
	private Integer gasUnitV3;

	@JsonProperty(JsonKeys.RESERVE_STORAGE_FEE)
	private Long reserveStorageFee;

	@JsonProperty(JsonKeys.DECIMALS)
	@NotNull(message = JsonKeys.DECIMALS + ValidationMessages.NOT_NULL)
	@Positive(message = JsonKeys.DECIMALS + Errors.MUST_BE_POSITIVE)
	private Integer decimals;
}

package com.speed.toncore.tokens.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.speed.toncore.constants.JsonKeys;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TonTokenResponse {

	@JsonProperty(JsonKeys.TOKEN_ADDRESS)
	private String tokenAddress;

	@JsonProperty(JsonKeys.TOKEN_NAME)
	private String tokenName;

	@JsonProperty(JsonKeys.TOKEN_SYMBOL)
	private String tokenSymbol;

	@JsonProperty(JsonKeys.NO_OF_CELL)
	private Integer noOfCell;

	@JsonProperty(JsonKeys.NO_OF_BITS)
	private Integer noOfBits;

	@JsonProperty(JsonKeys.GAS_UNIT)
	private Integer gasUnit;

	@JsonProperty(JsonKeys.NO_OF_CELL_V3)
	private Integer noOfCellV3;

	@JsonProperty(JsonKeys.NO_OF_BITS_V3)
	private Integer noOfBitsV3;

	@JsonProperty(JsonKeys.GAS_UNIT_V3)
	private Integer gasUnitV3;

	@JsonProperty(JsonKeys.DEPLOYMENT_COST)
	private Long deploymentCost;

	@JsonProperty(JsonKeys.RESERVE_STORAGE_FEE)
	private Long reserveStorageFee;

	@JsonProperty(JsonKeys.DECIMALS)
	private Integer decimals;
}

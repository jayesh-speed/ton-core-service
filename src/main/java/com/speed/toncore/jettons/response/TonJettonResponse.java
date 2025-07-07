package com.speed.toncore.jettons.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.speed.toncore.constants.JsonKeys;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TonJettonResponse {

	@JsonProperty(JsonKeys.JETTON_MASTER_ADDRESS)
	private String jettonMasterAddress;

	@JsonProperty(JsonKeys.JETTON_NAME)
	private String jettonName;

	@JsonProperty(JsonKeys.JETTON_SYMBOL)
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
	private Integer decimals;
}

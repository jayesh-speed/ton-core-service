package com.speed.toncore.jettons.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.speed.toncore.constants.JsonKeys;
import com.speed.toncore.util.TonRawAddressDeserializer;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TonJettonRequest {

	@JsonProperty(JsonKeys.JETTON_MASTER_ADDRESS)
	@JsonDeserialize(using = TonRawAddressDeserializer.class)
	private String jettonMasterAddress;

	@JsonProperty(JsonKeys.JETTON_NAME)
	private String jettonName;

	@JsonProperty(JsonKeys.JETTON_SYMBOL)
	private String jettonSymbol;

	@JsonProperty(JsonKeys.FORWARD_TON_AMOUNT)
	private BigDecimal forwardTonAmount;

	@JsonProperty(JsonKeys.DECIMALS)
	private int decimals;
}

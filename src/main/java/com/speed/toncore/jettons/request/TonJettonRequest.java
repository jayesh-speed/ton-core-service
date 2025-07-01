package com.speed.toncore.jettons.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.constants.JsonKeys;
import com.speed.toncore.constants.ValidationMessages;
import com.speed.toncore.util.TonRawAddressDeserializer;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TonJettonRequest {

	@JsonProperty(JsonKeys.JETTON_MASTER_ADDRESS)
	@JsonDeserialize(using = TonRawAddressDeserializer.class)
	@NotBlank(message = JsonKeys.JETTON_MASTER_ADDRESS + ValidationMessages.NOT_BLANK)
	private String jettonMasterAddress;

	@JsonProperty(JsonKeys.JETTON_NAME)
	@NotBlank(message = JsonKeys.JETTON_NAME + ValidationMessages.NOT_BLANK)
	private String jettonName;

	@JsonProperty(JsonKeys.JETTON_SYMBOL)
	@NotBlank(message = JsonKeys.JETTON_SYMBOL + ValidationMessages.NOT_BLANK)
	private String jettonSymbol;

	@JsonProperty(JsonKeys.FORWARD_TON_AMOUNT)
	@NotNull(message = Errors.FORWARD_TON_AMOUNT_REQUIRED)
	@DecimalMin(value = "0.05", message = Errors.INVALID_AMOUNT_VALUE)
	@Digits(integer = 19, fraction = 9, message = Errors.INVALID_FRACTIONAL)
	private BigDecimal forwardTonAmount;

	@JsonProperty(JsonKeys.DECIMALS)
	@NotNull(message = JsonKeys.DECIMALS + ValidationMessages.NOT_NULL)
	@Positive(message = JsonKeys.DECIMALS + Errors.MUST_BE_POSITIVE)
	private Integer decimals;
}

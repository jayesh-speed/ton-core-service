package com.speed.toncore.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.speed.toncore.constants.JsonKeys;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class FeeDto {

	@JsonProperty(JsonKeys.ChainStack.DESTINATION_FEES)
	private List<Fee> destinationFees;

	@JsonProperty(JsonKeys.ChainStack.SOURCE_FEES)
	private Fee sourceFees;

	@Setter
	@Getter
	public static class Fee {

		@JsonProperty(JsonKeys.ChainStack.FORWARD_FEE)
		private long fwdFee;

		@JsonProperty(JsonKeys.ChainStack.GAS_FEE)
		private long gasFee;

		@JsonProperty(JsonKeys.ChainStack.IN_FWD_FEE)
		private long inFwdFee;

		@JsonProperty(JsonKeys.ChainStack.STORAGE_FEE)
		private long storageFee;

	}
}

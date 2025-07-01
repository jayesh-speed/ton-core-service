package com.speed.toncore.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.speed.toncore.constants.JsonKeys;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class JettonTransferDto {

	@JsonProperty(JsonKeys.ChainStack.QUERY_ID)
	private String queryId;

	@JsonProperty(JsonKeys.ChainStack.SOURCE)
	private String source;

	@JsonProperty(JsonKeys.ChainStack.DESTINATION)
	private String destination;

	@JsonProperty(JsonKeys.ChainStack.AMOUNT)
	private String amount;

	@JsonProperty(JsonKeys.ChainStack.SOURCE_WALLET)
	private String sourceWallet;

	@JsonProperty(JsonKeys.ChainStack.JETTON_MASTER)
	private String jettonMaster;

	@JsonProperty(JsonKeys.ChainStack.TRANSACTION_HASH)
	private String transactionHash;

	@JsonProperty(JsonKeys.ChainStack.RESPONSE_DESTINATION)
	private String responseDestination;

	@JsonProperty(JsonKeys.ChainStack.CUSTOM_PAYLOAD)
	private String customPayload;

	@JsonProperty(JsonKeys.ChainStack.FORWARD_TON_AMOUNT)
	private String forwardTonAmount;

	@JsonProperty(JsonKeys.ChainStack.FORWARD_PAYLOAD)
	private String forwardPayload;

	@JsonProperty(JsonKeys.ChainStack.TRACE_ID)
	private String traceId;

	@JsonProperty(JsonKeys.ChainStack.TRANSACTION_LT)
	private Long transactionLt;

	@JsonProperty(JsonKeys.ChainStack.TRANSACTION_NOW)
	private Long transactionNow;

	@JsonProperty(JsonKeys.ChainStack.TRANSACTION_ABORTED)
	private Boolean transactionAborted;
}

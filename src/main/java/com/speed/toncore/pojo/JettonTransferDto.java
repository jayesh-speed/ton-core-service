package com.speed.toncore.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.speed.toncore.constants.JsonKeys;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JettonTransferDto {

	@JsonProperty(JsonKeys.TonIndexer.QUERY_ID)
	private String queryId;

	@JsonProperty(JsonKeys.TonIndexer.SOURCE)
	private String source;

	@JsonProperty(JsonKeys.TonIndexer.DESTINATION)
	private String destination;

	@JsonProperty(JsonKeys.TonIndexer.AMOUNT)
	private String amount;

	@JsonProperty(JsonKeys.TonIndexer.SOURCE_WALLET)
	private String sourceWallet;

	@JsonProperty(JsonKeys.TonIndexer.JETTON_MASTER)
	private String jettonMaster;

	@JsonProperty(JsonKeys.TonIndexer.TRANSACTION_HASH)
	private String transactionHash;

	@JsonProperty(JsonKeys.TonIndexer.RESPONSE_DESTINATION)
	private String responseDestination;

	@JsonProperty(JsonKeys.TonIndexer.CUSTOM_PAYLOAD)
	private String customPayload;

	@JsonProperty(JsonKeys.TonIndexer.FORWARD_TON_AMOUNT)
	private String forwardTonAmount;

	@JsonProperty(JsonKeys.TonIndexer.FORWARD_PAYLOAD)
	private String forwardPayload;

	@JsonProperty(JsonKeys.TonIndexer.TRACE_ID)
	private String traceId;

	@JsonProperty(JsonKeys.TonIndexer.TRANSACTION_LT)
	private Long transactionLt;

	@JsonProperty(JsonKeys.TonIndexer.TRANSACTION_NOW)
	private Long transactionNow;

	@JsonProperty(JsonKeys.TonIndexer.TRANSACTION_ABORTED)
	private Boolean transactionAborted;
}

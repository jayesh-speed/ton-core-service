package com.speed.toncore.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.speed.toncore.constants.JsonKeys;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
public class WalletInformationDto {

	@JsonProperty(JsonKeys.TonIndexer.BALANCE)
	private BigInteger balance;

	@JsonProperty(JsonKeys.TonIndexer.LAST_TRANSACTION_HASH)
	private String last_transaction_hash;

	@JsonProperty(JsonKeys.TonIndexer.LAST_TRANSACTION_LT)
	private String last_transaction_lt;

	@JsonProperty(JsonKeys.TonIndexer.SEQ_NO)
	private Integer seqNo;

	@JsonProperty(JsonKeys.TonIndexer.STATUS)
	private String status;

	@JsonProperty(JsonKeys.TonIndexer.WALLET_ID)
	private String walletId;

	@JsonProperty(JsonKeys.TonIndexer.WALLET_TYPE)
	private String walletType;
}

package com.speed.toncore.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.speed.toncore.constants.JsonKeys;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
public class WalletInformationDto {

	@JsonProperty(JsonKeys.ChainStack.BALANCE)
	private BigInteger balance;

	@JsonProperty(JsonKeys.ChainStack.LAST_TRANSACTION_HASH)
	private String last_transaction_hash;

	@JsonProperty(JsonKeys.ChainStack.LAST_TRANSACTION_LT)
	private String last_transaction_lt;

	@JsonProperty(JsonKeys.ChainStack.SEQ_NO)
	private Integer seqNo;

	@JsonProperty(JsonKeys.ChainStack.STATUS)
	private String status;

	@JsonProperty(JsonKeys.ChainStack.WALLET_ID)
	private String walletId;

	@JsonProperty(JsonKeys.ChainStack.WALLET_TYPE)
	private String walletType;
}

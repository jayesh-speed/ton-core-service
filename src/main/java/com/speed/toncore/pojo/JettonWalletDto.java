package com.speed.toncore.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.speed.toncore.constants.JsonKeys;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class JettonWalletDto {

	@JsonProperty(JsonKeys.ChainStack.JETTON_WALLETS)
	private List<JettonWallet> jettonWallets;

	@JsonProperty(JsonKeys.ChainStack.ADDRESS_BOOK)
	private Map<String, AddressBookEntry> addressBook;

	@Getter
	@Setter
	public static class JettonWallet {

		@JsonProperty(JsonKeys.ChainStack.ADDRESS)
		private String address;

		@JsonProperty(JsonKeys.ChainStack.BALANCE)
		private BigInteger balance;

		@JsonProperty(JsonKeys.ChainStack.OWNER)
		private String owner;

		@JsonProperty(JsonKeys.ChainStack.JETTON)
		private String jetton;

		@JsonProperty(JsonKeys.ChainStack.LAST_TRANSACTION_LT)
		private String lastTransactionLt;

		@JsonProperty(JsonKeys.ChainStack.CODE_HASH)
		private String codeHash;

		@JsonProperty(JsonKeys.ChainStack.DATA_HASH)
		private String dataHash;
	}

	@Getter
	@Setter
	public static class AddressBookEntry {

		@JsonProperty(JsonKeys.ChainStack.USER_FRIENDLY)
		private String userFriendly;
	}
}

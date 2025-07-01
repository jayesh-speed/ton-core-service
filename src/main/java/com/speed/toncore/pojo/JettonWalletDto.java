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

	@JsonProperty(JsonKeys.TonIndexer.JETTON_WALLETS)
	private List<JettonWallet> jettonWallets;

	@JsonProperty(JsonKeys.TonIndexer.ADDRESS_BOOK)
	private Map<String, AddressBookEntry> addressBook;

	@Getter
	@Setter
	public static class JettonWallet {

		@JsonProperty(JsonKeys.TonIndexer.ADDRESS)
		private String address;

		@JsonProperty(JsonKeys.TonIndexer.BALANCE)
		private BigInteger balance;

		@JsonProperty(JsonKeys.TonIndexer.OWNER)
		private String owner;

		@JsonProperty(JsonKeys.TonIndexer.JETTON)
		private String jetton;

		@JsonProperty(JsonKeys.TonIndexer.LAST_TRANSACTION_LT)
		private String lastTransactionLt;

		@JsonProperty(JsonKeys.TonIndexer.CODE_HASH)
		private String codeHash;

		@JsonProperty(JsonKeys.TonIndexer.DATA_HASH)
		private String dataHash;
	}

	@Getter
	@Setter
	public static class AddressBookEntry {

		@JsonProperty(JsonKeys.TonIndexer.USER_FRIENDLY)
		private String userFriendly;
	}
}

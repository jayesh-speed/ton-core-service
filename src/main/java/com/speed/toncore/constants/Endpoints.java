package com.speed.toncore.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Endpoints {

	public final String ADD_TON_TOKEN = "/token/add";
	public final String CREATE_FEE_ACCOUNT = "/account/ton-fee/create";
	public final String CREATE_MAIN_ACCOUNT = "/account/ton-main/create";
	public final String CREATE_TON_LISTENER = "/ton-listener/create";
	public final String CREATE_TON_ADDRESSES = "/account/ton-wallet-address/create";
	public final String DEPLOY_FEE_ACCOUNT = "/account/ton-fee/deploy/{address}";
	public final String DEPLOY_MAIN_ACCOUNT = "/account/ton-main/deploy/{address}";
	public final String ESTIMATE_TRANSACTION_FEE = "/transaction-fee/estimate";
	public final String GET_ALL_TOKENS = "/token/all";
	public final String GET_FEE_ACCOUNTS = "/account/ton-fee/get";
	public final String GET_TOKENS = "/token/{address}";
	public final String GET_TOKEN_BALANCE = "/token-balance/{tokenAddress}/{ownerAddress}";
	public final String GET_TOKEN_BY_SYMBOL = "/token/symbol/{symbol}";
	public final String GET_MAIN_ACCOUNTS = "/account/ton-main/get/{tokenAddress}";
	public final String GET_TON_BALANCE = "/account/balance/{address}";
	public final String GET_TON_ADDRESS = "/account/ton-wallet-address/get";
	public final String INITIATE_SWEEP = "/sweep/initiate/{id}";
	public final String TOKEN_TRANSFER_FEE = "/transaction-fee/token";
	public final String REMOVE_FEE_ACCOUNT = "/account/ton-fee/remove/{address}";
	public final String REMOVE_MAIN_ACCOUNT = "/account/ton-main/remove/{address}";
	public final String REMOVE_TON_TOKEN = "/token/remove/{address}";
	public final String REMOVE_TON_LISTENER_BY_ID = "/ton-listener/remove/{id}";
	public final String REMOVE_USED_TON_ADDRESS = "/account/ton-wallet-address/remove/{address}";
	public final String SWEEP_FEE = "/transaction-fee/sweep";
	public final String TRANSFER_TOKEN = "/payment/send-token";
	public final String UPDATE_LOGICAL_TIME = "/payment/logical-time/update/{id}/{logicalTime}";
	public final String UPDATE_TON_LISTENER = "/ton-listener/update/{id}";

	@UtilityClass
	public static class TonIndexer {

		public final String GET_ACCOUNT_STATE = "/api/v2/getAddressState";
		public final String GET_CONFIG_PARAM = "/api/v2/getConfigParam";
		public final String GET_ESTIMATE_FEES = "/api/v3/estimateFee";
		public final String GET_TOKEN_TRANSFERS = "/api/v3/jetton/transfers";
		public final String GET_TOKEN_CONTRACT = "/api/v3/jetton/wallets";
		public final String GET_TON_BALANCE = "/api/v2/getAddressBalance";
		public final String GET_TRACE_BY_TRACE_ID = "/api/v3/traces";
		public final String GET_WALLET_INFORMATION = "/api/v3/walletInformation";
		public final String SEND_MESSAGE_WITH_RETURN_DATA = "/api/v3/message";
	}
}

package com.speed.toncore.constants;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.BigInteger;

@UtilityClass
public class Constants {

	public final Integer MAIN_NET_CHAIN_ID = 1;
	public final Integer TEST_NET_CHAIN_ID = 11;
	public final Long ONE_BILLION = 1_000_000_000L;
	public final String ACCOUNT_TYPE = "speed-account-type";
	public final String ACTIVE = "active";
	public final String API_CALL = "api_call";
	public final String API_ERROR = "api_error";
	public final String CACHE_KEY_GENERATOR = "cachingKeyGenerator";
	public final String CHAIN_ID = "chain-id";
	public final String ELAPSE_TIME = "elapse_time";
	public final String FALSE = "false";
	public final String MAIN_NET = "main_net";
	public final String MODE = "speed-livemode";
	public final String PROCESSED_BY_TON = "ton";
	public final String REQUEST_ID = "speed-request";
	public final String TON_WITHDRAW_METHOD = "ton";
	public final String USDT_CURRENCY_SYMBOL = "USDT";
	public final String USER_ID = "user_id";
	public final String WALLET = "wallet";
	public final String X_API_KEY = "X-Api-Key";
	public final int MAIN_NET_POLLING_INTERVAL = 10;
	public final int TEST_NET_POLLING_INTERVAL = 15;
	public final BigDecimal DEFAULT_TRANSACTION_FEE = new BigDecimal("0.03942688");
	public final BigDecimal DEFAULT_SWEEP_TRANSACTION_FEE = new BigDecimal("0.03942688");


	@UtilityClass
	public class SecretManagerKeys {

		public final String API_KEY = "api-key";
		public final String BASE_URL = "base-url";
		public final String ENCRYPTION_ALGO = "encryption-algo";
		public final String ENCRYPTION_KEY = "encryption-key";
		public final String LISTENER_API_KEY = "listener-api-key";
		public final String LISTENER_BASE_URL = "listener-base-url";
		public final String TON_CENTER_API_KEY = "ton-center-api-key";
		public final String TON_CENTER_URL = "ton-center-url";
		public final String WALLET_ID = "wallet-id";
	}

	@UtilityClass
	public class CacheNames {

		public final String JETTON_RESPONSE = "jettonResponse";
		public final String RECEIVE_ADDRESSES = "receiveAddresses";
		public final String SEND_ADDRESSES = "sendAddresses";
	}

	@UtilityClass
	public static class Events {

		public final String ADD_TON_JETTON = "addTonJetton";
		public final String CREATE_FEE_ACCOUNT = "createFeeAccount";
		public final String CREATE_MAIN_ACCOUNT = "createMainAccount";
		public final String CREATE_TON_LISTENER = "createTonListener";
		public final String CREATE_TON_WALLET_ADDRESSES = "createTonWalletAddresses";
		public final String DEPLOY_FEE_ACCOUNT = "deployFeeAccount";
		public final String DEPLOY_MAIN_ACCOUNT = "deployMainAccount";
		public final String GET_ALL_JETTONS = "getAllJettons";
		public final String GET_FEE_ACCOUNTS = "getFeeAccounts";
		public final String GET_JETTON = "getJetton";
		public final String GET_JETTON_BALANCE = "getJettonBalance";
		public final String GET_JETTON_BY_SYMBOL = "getJettonBySymbol";
		public final String GET_MAIN_ACCOUNT = "getMainAccount";
		public final String GET_TON_BALANCE = "getTonBalance";
		public final String GET_TON_WALLET_ADDRESS = "getTonWalletAddress";
		public final String JETTON_TRANSACTION_FEE = "JettonTransactionFee";
		public final String REMOVE_FEE_ACCOUNT = "removeFeeAccount";
		public final String REMOVE_MAIN_ACCOUNT = "removeMainAccount";
		public final String REMOVE_TON_JETTON = "removeTonJetton";
		public final String REMOVE_TON_LISTENER = "removeTonListener";
		public final String REMOVE_USED_TON_WALLET_ADDRESS = "removeUsedTonWalletAddress";
		public final String SWEEP_TRANSACTION_FEE = "SweepTransactionFee";
		public final String TRANSFER_JETTON = "transferJetton";
		public final String UPDATE_LOGICAL_TIME = "updateLogicalTime";
		public final String UPDATE_JETTON_WALLET = "updateJettonWallet";
		public final String UPDATE_TON_LISTENER_STATUS = "updateTonListenerStatus";
		public final String ESTIMATE_TRANSACTION_FEE = "estimateTransactionFee";
	}

	@UtilityClass
	public class ConsumerGroupIds {

		public final String SCHEDULER_GROUP = "ton_scheduler_group";
		public final String TON_USDT_PAYOUT_LIVE = "ton_usdt_payout_live";
		public final String TON_USDT_PAYOUT_TEST = "ton_usdt_payout_test";
		public final String TON_USDT_WITHDRAW_LIVE = "ton_usdt_withdraw_live";
		public final String TON_USDT_WITHDRAW_TEST = "ton_usdt_withdraw_test";
	}
}

package com.speed.toncore.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

	public final String ACCOUNT_TYPE = "speed-account-type";
	public final String ACTIVE = "active";
	public final String API_CALL = "api_call";
	public final String API_ERROR = "api_error";
	public final String CACHE_KEY_GENERATOR = "cachingKeyGenerator";
	public final String CHAIN_ID = "chain-id";
	public final String ELAPSE_TIME = "elapse_time";
	public final String INSUFFICIENT_FEE_ERROR = "insufficient funds for gas";
	public final String EVENT = "event";
	public final String EXIT_CODE_33 = "exit_code_33";
	public final String FALSE = "false";
	public final String PROCESSED_BY_TON = "ton";
	public final String TON_WITHDRAW_METHOD = "ton";
	public final String FORWARD_TON_AMOUNT_FOR_DEPLOYMENT = "0.1";
	public final String FORWARD_TON_AMOUNT_FOR_JETTON_TRANSFER = "0.1";
	public final String FROM_ADDRESS = "fromAddress";
	public final String JETTON_ADDRESS = "jetton_address";
	public final String MAIN_ACCOUNT_ADDRESS = "main_account_address";
	public final String MAIN_NET = "main_net";
	public final String MODE = "speed-livemode";
	public final String REQUEST_ID = "speed-request";
	public final String SPENDER_ADDRESS = "spenderAddress";
	public final String TO_ADDRESS = "toAddress";
	public final String TO_MANY_EXTERNAL_MASSAGE = "too many external messages to address";
	public final String UNPACK_ACCOUNT_STATE = "failed to unpack account state";
	public final String USDT_CURRENCY_SYMBOL = "USDT";
	public final String USER_ID = "user_id";
	public final String VALUE = "value";
	public final String WALLET = "wallet";
	public final int MAIN_NET_POLLING_INTERVAL = 10;
	public final int TEST_NET_POLLING_INTERVAL = 15;
	public final Integer MAIN_NET_CHAIN_ID = 1;
	public final Integer TEST_NET_CHAIN_ID = 11;

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

		public final String RECEIVE_ADDRESSES = "receiveAddresses";
		public final String SEND_ADDRESSES = "sendAddresses";
		public final String JETTON_RESPONSE = "jettonResponse";
	}

	@UtilityClass
	public static class Events {

		public final String ADD_TON_JETTON = "addTonJetton";
		public final String CREATE_FEE_ACCOUNT = "deployFeeAccount";
		public final String CREATE_MAIN_ACCOUNT = "createMainAccount";
		public final String CREATE_TON_LISTENER = "createTonListener";
		public final String CREATE_TON_WALLET_ADDRESSES = "createTonWalletAddresses";
		public final String DEPLOY_FEE_ACCOUNT = "deployFeeAccount";
		public final String DEPLOY_MAIN_ACCOUNT = "deployMainAccount";
		public final String GET_ALL_JETTONS = "getAllJettons";
		public final String GET_FEE_ACCOUNT = "getFeeAccount";
		public final String GET_FEE_ACCOUNT_BALANCE = "getFeeAccountBalance";
		public final String GET_JETTON = "getJetton";
		public final String GET_JETTON_BALANCE = "getJettonBalance";
		public final String GET_JETTON_BY_SYMBOL = "getJettonBySymbol";
		public final String GET_MAIN_ACCOUNT = "getMainAccount";
		public final String GET_MAIN_ACCOUNT_BALANCE = "getMainAccountBalance";
		public final String GET_TON_BALANCE = "getTonBalance";
		public final String GET_TON_WALLET_ADDRESS = "getTonWalletAddress";
		public final String JETTON_TRANSFER = "jettonTransfer";
		public final String REMOVE_FEE_ACCOUNT = "removeFeeAccount";
		public final String REMOVE_MAIN_ACCOUNT = "removeMainAccount";
		public final String REMOVE_TON_JETTON = "removeTonJetton";
		public final String REMOVE_TON_LISTENER = "removeTonListener";
		public final String REMOVE_USED_TON_WALLET_ADDRESS = "removeUsedTonWalletAddress";
		public final String TRANSFER_JETTON = "transferJetton";
		public final String UPDATE_FEE_ACCOUNT_BALANCE = "updateFeeAccountBalance";
		public final String UPDATE_LOGICAL_TIME = "updateLogicalTime";
		public final String UPDATE_MAIN_ACCOUNT_BALANCE = "updateMainAccountBalance";
		public final String UPDATE_TON_LISTENER_STATUS = "updateTonListenerStatus";
	}

	@UtilityClass
	public class ConsumerGroupIds {

		public final String TON_GROUP = "ton_group";
		public final String TON_USDT_PAYOUT_LIVE = "ton_usdt_payout_live";
		public final String TON_USDT_PAYOUT_TEST = "ton_usdt_payout_test";
		public final String TON_USDT_WITHDRAW_LIVE = "ton_usdt_withdraw_live";
		public final String TON_USDT_WITHDRAW_TEST = "ton_usdt_withdraw_test";
		public final String SCHEDULER_GROUP = "ton_scheduler_group";
	}
}

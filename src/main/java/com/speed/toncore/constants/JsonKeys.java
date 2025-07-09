package com.speed.toncore.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class JsonKeys {

	public final String ADDRESS = "address";
	public final String BALANCE = "balance";
	public final String CHAIN_ID = "chain_id";
	public final String COUNT = "count";
	public final String DECIMALS = "decimals";
	public final String DEPLOYMENT_COST = "deployment_cost";
	public final String ERRORS = "errors";
	public final String FROM_ADDRESS = "from_address";
	public final String FWD_FEE = "fwd_fee";
	public final String GAS_UNIT = "gas_unit";
	public final String GAS_UNIT_V3 = "gas_unit_v3";
	public final String IN_MSG = "in_msg";
	public final String IS_MAIN_NET = "is_main_net";
	public final String JETTON_MASTER_ADDRESS = "jetton_master_address";
	public final String JETTON_NAME = "jetton_name";
	public final String JETTON_SYMBOL = "jetton_symbol";
	public final String LOCAL_BALANCE = "local_balance";
	public final String NO_OF_BITS = "no_of_bits";
	public final String NO_OF_BITS_V3 = "no_of_bits_v3";
	public final String NO_OF_CELL = "no_of_cell";
	public final String NO_OF_CELL_V3 = "no_of_cell_v3";
	public final String OUT_MSGS = "out_msgs";
	public final String PARAMETERS = "params";
	public final String RESERVE_STORAGE_FEE = "reserve_storage_fee";
	public final String SERVER_NAME = "server_name";
	public final String SERVER_PATH = "server_path";
	public final String STATUS_CODE = "status_code";
	public final String TOTAL_FEES = "total_fees";
	public final String TO_ADDRESS = "to_address";
	public final String TRACES = "traces";
	public final String TRACE_ID = "trace_id";
	public final String TRANSACTIONS = "transactions";
	public final String TRANSACTIONS_ORDER = "transactions_order";
	public final String TRANSACTION_FEE = "transaction_fee";
	public final String ESTIMATE_FEE = "estimate_fee";
	public final String TRANSACTION_HASH = "transaction_hash";
	public final String TX_REFERENCE = "tx_reference";
	public final String TYPE = "type";
	public final String VALUE = "value";

	@UtilityClass
	public static class QueryParameters {

		public final String ADDRESS = "address";
		public final String BOC = "boc";
		public final String BODY = "body";
		public final String CONFIG_ID = "config_id";
		public final String EXCLUDE_ZERO_BALANCE = "exclude_zero_balance";
		public final String IGNORE_CHKSIG = "ignore_chksig";
		public final String INCLUDE_ACTIONS = "include_actions";
		public final String INIT_CODE = "init_code";
		public final String INIT_DATA = "init_data";
		public final String JETTON_MASTER = "jetton_master";
		public final String JETTON_MASTER_ADDRESS = "jetton_address";
		public final String LIMIT = "limit";
		public final String OFFSET = "offset";
		public final String OWNER_ADDRESS = "owner_address";
		public final String SORT = "sort";
		public final String SORT_ASC = "asc";
		public final String START_LT = "start_lt";
		public final String TRACE_ID = "trace_id";
		public final String USE_V2 = "use_v2";
	}

	@UtilityClass
	public static class TonIndexer {

		public final String ADDRESS = "address";
		public final String ADDRESS_BOOK = "address_book";
		public final String AMOUNT = "amount";
		public final String BALANCE = "balance";
		public final String CODE_HASH = "code_hash";
		public final String CUSTOM_PAYLOAD = "custom_payload";
		public final String DATA_HASH = "data_hash";
		public final String DESTINATION = "destination";
		public final String DESTINATION_FEES = "destination_fees";
		public final String FORWARD_FEE = "fwd_fee";
		public final String FORWARD_PAYLOAD = "forward_payload";
		public final String FORWARD_TON_AMOUNT = "forward_ton_amount";
		public final String GAS_FEE = "gas_fee";
		public final String IN_FWD_FEE = "in_fwd_fee";
		public final String JETTON = "jetton";
		public final String JETTON_MASTER = "jetton_master";
		public final String JETTON_TRANSFERS = "jetton_transfers";
		public final String JETTON_WALLETS = "jetton_wallets";
		public final String LAST_TRANSACTION_HASH = "last_transaction_hash";
		public final String LAST_TRANSACTION_LT = "last_transaction_lt";
		public final String MESSAGE_HASH = "message_hash";
		public final String MESSAGE_HASH_NORM = "message_hash_norm";
		public final String OK = "ok";
		public final String OWNER = "owner";
		public final String QUERY_ID = "query_id";
		public final String RESPONSE_DESTINATION = "response_destination";
		public final String RESULT = "result";
		public final String SEQ_NO = "seqno";
		public final String SOURCE = "source";
		public final String SOURCE_FEES = "source_fees";
		public final String SOURCE_WALLET = "source_wallet";
		public final String STATUS = "status";
		public final String STORAGE_FEE = "storage_fee";
		public final String TRACE_ID = "trace_id";
		public final String TRANSACTION_ABORTED = "transaction_aborted";
		public final String TRANSACTION_HASH = "transaction_hash";
		public final String TRANSACTION_LT = "transaction_lt";
		public final String TRANSACTION_NOW = "transaction_now";
		public final String USER_FRIENDLY = "user_friendly";
		public final String WALLET_ID = "wallet_id";
		public final String WALLET_TYPE = "wallet_type";
	}
}

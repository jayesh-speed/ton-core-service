package com.speed.toncore.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Errors {

	public final String ACCOUNT_ALREADY_DEPLOYED = "account already deployed for address %s";
	public final String BLANK_ADDRESS = "Address cannot be blank";
	public final String BLANK_JETTON_MASTER_ADDRESS = "Jetton master address cannot be blank";
	public final String CONFIRM_ON_CHAIN_TX_UPDATE_FAIL = "Failed to update confirmed on chain tx for address: %s and jetton: %s with txHash: %s";
	public final String CONNECTION_ERROR_WITH_SECRET_MANAGER = "Connection error with secret manager due to: %s";
	public final String CREDIT_ON_CHAIN_TX_CONSTRAINT_VIOLATION = "Got Constraint violation exception while inserting confirmed credit transaction for address: %s with txHash: %s";
	public final String DEBIT_ON_CHAIN_TX_CONSTRAINT_VIOLATION = "Got Constraint violation exception while inserting confirmed debit transaction for address: %s with txHash: %s";
	public final String DECRYPTION_EXCEPTION = "Exception occurred during decryption";
	public final String EMPTY_KAFKA_MESSAGE_RECEIVED = "Empty record received. Skipping the message.";
	public final String ENCRYPTION_EXCEPTION = "Exception occurred during encryption";
	public final String ERROR_DEPLOY_FEE_ACCOUNT = "Failed to deploy fee account for address %s and chainId %s";
	public final String ERROR_DEPLOY_MAIN_ACCOUNT = "Failed to deploy main account for address %s and chainId %s";
	public final String ERROR_FETCHING_JETTON_WALLET = "Error while fetching jetton wallet for address %s and jetton master %s";
	public final String ERROR_FUND_TRANSFER_TO_MAIN_ACCOUNT = "Failed to transfer fund to main account for address %s";
	public final String ERROR_JETTON_TRANSFER = "Error in transferring jetton %s from %s to %s";
	public final String ERROR_ON_CHAIN_RAW_TX = "Error while performing on-chain transaction for chainId : %s";
	public final String ERROR_ON_FETCHING_TRACE = "Error while fetching trace for traceId: %s";
	public final String ERROR_ON_SWEEP_TRANSACTION = "Error while performing sweep transaction for chainId : %s";
	public final String ERROR_PAYOUT_AMOUNT = "Invalid payout amount. Your request cannot be completed";
	public final String ERROR_PAYOUT_METHOD = "Invalid payout method. Your request cannot be completed";
	public final String ERROR_PAYOUT_REQUEST = "Empty payout request(to_address). Your request cannot be completed";
	public final String ERROR_PROCESSING_PAYOUT = "Error while processing payout";
	public final String ERROR_PROCESSING_WITHDRAW = "Error while processing withdraw";
	public final String ERROR_WHILE_FETCHING_CONFIG_PARAMS = "Failed to fetch or process config param %s for chain %s";
	public final String ERROR_SUBSCRIBE_JETTON_LISTENER = "Error in rebooting the on chain jetton listener. Shutting down the current listener.";
	public final String ERROR_SUBSCRIBING_ONCHAIN_TRANSACTION = "Error while subscribing on chain transaction for listener id: %s";
	public final String ERROR_WHILE_CREATING_WALLETS = "Error creating new wallet addresses : %s";
	public final String ERROR_WHILE_ESTIMATION_FEE = "Error while estimating fee for chainId %s";
	public final String ERROR_WHILE_PARSING_MESSAGE = "Error while parsing message";
	public final String ERROR_WITHDRAW_AMOUNT = "Invalid withdraw amount. Your request cannot be completed";
	public final String ERROR_WITHDRAW_METHOD = "Invalid withdraw method. Your request cannot be completed";
	public final String ERROR_WITHDRAW_REQUEST = "Empty withdraw request(to_address). Your request cannot be completed";
	public final String ERROR_WITH_ACCOUNT_TYPE = "Invalid account type. Your request cannot be completed";
	public final String FAILED_TO_DESERIALIZE_ADDRESS = "Failed to deserialize address: %s";
	public final String FEE_ACCOUNT_NOT_FOUND = "Fee account not found for address: %s and chainId: %s";
	public final String INSUFFICIENT_FEE_BALANCE = "We are having trouble on our end sending this %s transaction. Please try again after sometime.";
	public final String INSUFFICIENT_MAIN_ACC_BALANCE = "We are having trouble on our end sending this %s transaction. Please try again after sometime.";
	public final String INTERNAL_SERVER_ERROR = "Internal server error, please report with requestId: %s";
	public final String INVALID_ADDRESS = "Invalid TON address: %s";
	public final String INVALID_PARAMS = "Invalid request param : %s";
	public final String INVALID_REQUEST = "The request was invalid due to: %s";
	public final String JETTON_ADDRESS_NOT_SUPPORTED = "Provided jetton address is not supported. Please provide a valid jetton address.";
	public final String JETTON_ALREADY_EXISTS = "Jetton already exists for address %s and chainId %s";
	public final String JETTON_INFO_MISSING = "Missing jetton address and jetton symbol in transfer request";
	public final String JETTON_SYMBOL_NOT_SUPPORTED = "Provided jetton symbol is not supported. Please provide a valid jetton symbol.";
	public final String JETTON_WALLET_NOT_FOUND = "Jetton wallet not found for address %s and jetton master %s";
	public final String LISTENER_NOT_FOUND = "Listener not found for id: %s";
	public final String MAIN_ACCOUNT_NOT_FOUND = "Speed ton main account not found for address: %s and chainId: %s";
	public final String MISSING_HEADER = "Header missing: %s";
	public final String MUST_BE_POSITIVE = " must be positive";
	public final String NODE_NOT_FOUND = "Node not found for requested chainId: %s";
	public final String NOT_ELIGIBLE_FOR_SWEEP = "Cannot perform sweep for address %s";
	public final String NOT_ENOUGH_FUNDS_TO_DEPLOY = "Not enough funds to deploy account for address %s";
	public final String PARAMETER_MISSING = "%s parameter is missing";
	public final String PRIVATE_KEY_NOT_DECRYPTED = "Could not get the decrypted private key";
	public final String REST_CLIENT_API_ERROR = "Rest client api error";
	public final String RETRYING_TO_FETCH_JETTON_WALLET = "Retrying to fetch jetton wallet for address %s and jetton master %s";
	public final String SECRET_VALUE_NOT_FOUND = "Unable to get value of given secretId: %s";
	public final String UNSUPPORTED_MEDIA_TYPE = "%s media type is not supported. Supported media types are %s";
	public final String WARN_CREATE_IN_PROGRESS = "Create request already in progress so not triggering second create flow";
	public final String WITHDRAW_PROCESS_EXIST = "Withdraw process already exists with withdraw id %s and account id %s";

	@UtilityClass
	public static class TonIndexer {

		public final String DROPPED_TRANSFERS_ERROR = "Dropped transfer due to backpressure: {}";
		public final String ELAPSE_TIME = "PollTransfers took {} ms";
		public final String EMPTY_RESPONSE_BODY = "Expected response body but received empty response";
		public final String ERROR_ON_FETCHING_JETTON_TRANSFERS = "Error while fetching jetton transfers for jetton master %s response: %s status: %s";
		public final String EXIT_CODE_33 = "exit_code_33";
		public final String FAILED_TO_GET_SEQ_NO = "Failed to get seq no for address %s";
		public final String FAILED_TO_PARSE_RESPONSE = "Failed to parse response: %s";
		public final String FAILED_TO_PARSE_TRANSFER = "Failed to parse transfer JSON: %s";
		public final String MISSING_TRANSFERS_FIELD = "Response missing 'jetton_transfers'. Skipping...";
		public final String NULL_STATUS_CODE = "Response status code is null";
		public final String TON_INDEXER_INTERNAL_SERVER_ERROR = "Internal server error status  %s response  %s";
		public final String TOO_MANY_EXTERNAL_MESSAGE = "too many external messages to address";
		public final String TOO_MANY_REQUEST_ERROR = "Too many request. Retrying...";
		public final String UNEXPECTED_ERROR = "Unexpected error status %s response %s";
		public final String UNEXPECTED_ERROR_ON_FETCH_TRANSFERS = "Unexpected error while fetching transfers for jetton : %s";
		public final String UNPACK_ACCOUNT_STATE = "failed to unpack account state";
	}
}

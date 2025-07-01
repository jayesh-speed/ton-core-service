package com.speed.toncore.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class LogMessages {

	@UtilityClass
	public class Warn {
		public final String ZERO_ACCOUNT_BALANCE = "Zero account balance for address: %s jetton master address: %s";
		public final String LISTENER_ALREADY_RUNNING = "Ton listeners updated to Running status concurrently with another request.\n {}";
		public final String LISTENER_NOT_FOUND = "Ton listener not found for chainId: {}";
	}

	@UtilityClass
	public class Info {
		public final String WAITING_FOR_BALANCE_UPDATE = "Awaiting jetton balance update for address: %s";
		public final String TRANSACTION_HASH = "Transaction hash: %s";
		public final String PAYOUT_LIVE_CONSUMER_COMPLETED = "Payout live mode consumer completed processing.";
		public final String PAYOUT_LIVE_CONSUMER_INVOKED = "%s payout live mode consumer invoked.";
		public final String PAYOUT_TEST_CONSUMER_COMPLETED = "Payout test mode consumer completed processing.";
		public final String PAYOUT_TEST_CONSUMER_INVOKED = "%s payout test mode consumer invoked.";
		public final String WITHDRAW_LIVE_CONSUMER_COMPLETED = "Withdraw live mode consumer completed processing.";
		public final String WITHDRAW_LIVE_CONSUMER_INVOKED = "%s withdraw live mode consumer invoked.";
		public final String WITHDRAW_TEST_CONSUMER_COMPLETED = "Withdraw test mode consumer completed processing.";
		public final String WITHDRAW_TEST_CONSUMER_INVOKED = "%s withdraw test mode consumer invoked.";
	}
}

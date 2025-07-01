package com.speed.toncore.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.speed.toncore.constants.JsonKeys;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TraceDto {

	@JsonProperty(JsonKeys.TRACES)
	private List<Trace> traces;

	@Data
	public static class Trace {

		@JsonProperty(JsonKeys.TRANSACTIONS_ORDER)
		private List<String> transactionsOrder;

		@JsonProperty(JsonKeys.TRANSACTIONS)
		private Map<String, Transaction> transactions;

		@Data
		public static class Transaction {

			@JsonProperty(JsonKeys.TOTAL_FEES)
			private String totalFees;

			@JsonProperty(JsonKeys.IN_MSG)
			private Message inMsg;

			@JsonProperty(JsonKeys.OUT_MSGS)
			private List<Message> outMsgs;

			@Data
			public static class Message {

				@JsonProperty(JsonKeys.VALUE)
				private String value;

				@JsonProperty(JsonKeys.FWD_FEE)
				private String fwdFee;
			}
		}
	}
}

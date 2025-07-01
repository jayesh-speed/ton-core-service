package com.speed.toncore.pojo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TraceDto {

	private List<Trace> traces;

	@Data
	public static class Trace {

		private List<String> transactions_order;
		private Map<String, Transaction> transactions;

		@Data
		public static class Transaction {

			private String total_fees;
			private Message in_msg;
			private List<Message> out_msgs;

			@Data
			public static class Message {

				private String value;
				private String fwd_fee;
			}
		}
	}
}

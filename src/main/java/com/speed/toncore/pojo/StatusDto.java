package com.speed.toncore.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.speed.toncore.constants.JsonKeys;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StatusDto {

	@JsonProperty(JsonKeys.TonIndexer.STACK)
	private List<StackItem> stack;

	@Getter
	@Setter
	public static class StackItem {

		@JsonProperty(JsonKeys.TonIndexer.TYPE)
		private String type;
		@JsonProperty(JsonKeys.TonIndexer.VALUE)
		private String value;
	}
}

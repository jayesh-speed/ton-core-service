package com.speed.toncore.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.speed.toncore.constants.JsonKeys;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountStatusDto {

	@JsonProperty(JsonKeys.TonIndexer.OK)
	private boolean ok;

	@JsonProperty(JsonKeys.TonIndexer.RESULT)
	private String result;
}

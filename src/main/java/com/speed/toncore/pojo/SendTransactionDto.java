package com.speed.toncore.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.speed.toncore.constants.JsonKeys;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendTransactionDto {

	@JsonProperty(JsonKeys.TonIndexer.MESSAGE_HASH)
	private String messageHash;

	@JsonProperty(JsonKeys.TonIndexer.MESSAGE_HASH_NORM)
	private String messageHashNorm;

}

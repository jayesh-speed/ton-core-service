package com.speed.toncore.accounts.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.speed.toncore.constants.JsonKeys;
import com.speed.toncore.constants.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionFeeRequest {

	@NotBlank(message = JsonKeys.TRACE_ID + ValidationMessages.NOT_BLANK)
	@JsonProperty(JsonKeys.TRACE_ID)
	private String traceId;
}

package com.speed.toncore.accounts.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.constants.JsonKeys;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TonWalletRequest {

	@JsonProperty(JsonKeys.COUNT)
	@Positive(message = JsonKeys.COUNT + Errors.MUST_BE_POSITIVE)
	private Integer count;
}

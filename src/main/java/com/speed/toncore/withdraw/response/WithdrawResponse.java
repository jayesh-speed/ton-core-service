package com.speed.toncore.withdraw.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.speed.toncore.constants.JsonKeys;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WithdrawResponse {

    @JsonProperty(JsonKeys.TRANSACTION_HASH)
    private String transactionHash;

    @JsonProperty(JsonKeys.TX_REFERENCE)
    private String txReference;

    @JsonProperty(JsonKeys.FROM_ADDRESS)
    private String fromAddress;

    @JsonProperty(JsonKeys.TO_ADDRESS)
    private String toAddress;

    @JsonProperty(JsonKeys.VALUE)
    private String value;
}

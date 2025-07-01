package com.speed.toncore.pojo;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class WithdrawProcessPojo {

    private String referenceId;
    private String referenceType;
    private String withdrawRequest;
    private String accountId;
    private BigDecimal targetAmount;
    private String targetCurrency;
    private String withdrawType;
}

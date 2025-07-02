package com.speed.toncore.accounts.service;

import java.math.BigDecimal;

public interface TransactionFeeService {

	BigDecimal getJettonTransactionFee(String traceId);

	BigDecimal getSweepTransactionFee(String traceId);
}

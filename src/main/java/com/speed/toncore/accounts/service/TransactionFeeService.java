package com.speed.toncore.accounts.service;

import com.speed.toncore.accounts.request.FeeEstimationRequest;
import com.speed.toncore.accounts.response.EstimateFeeResponse;

import java.math.BigDecimal;

public interface TransactionFeeService {

	BigDecimal getJettonTransactionFee(String traceId);

	BigDecimal getSweepTransactionFee(String traceId);

	EstimateFeeResponse estimateManualTransactionFee(FeeEstimationRequest request);
}

package com.speed.toncore.accounts.service;

import com.speed.toncore.accounts.request.FeeEstimationRequest;
import com.speed.toncore.accounts.response.EstimateFeeResponse;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface TransactionFeeService {

	BigDecimal getJettonTransactionFee(String traceId);

	BigDecimal getSweepTransactionFee(String traceId);

	EstimateFeeResponse estimateTransactionFee(FeeEstimationRequest request);

	BigInteger estimateSweepFee(String feeAccountAddress, String spenderAccountAddress, String mainAccountJettonAddress, String jettonMasterAddress);
}

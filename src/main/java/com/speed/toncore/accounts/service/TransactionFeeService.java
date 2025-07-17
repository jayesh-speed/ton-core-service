package com.speed.toncore.accounts.service;

import com.speed.toncore.accounts.request.FeeEstimationRequest;
import com.speed.toncore.accounts.response.EstimateFeeResponse;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface TransactionFeeService {

	BigDecimal getTokenTransferFee(String traceId);

	BigDecimal getSweepFee(String traceId);

	EstimateFeeResponse estimateTransactionFee(FeeEstimationRequest request);

	BigInteger estimateSweepFee(String feeAccountAddress, String spenderAccountAddress, String mainAccountTokenContractAddress, String tokenAddress);
}

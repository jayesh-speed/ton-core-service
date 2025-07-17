package com.speed.toncore.service;

import com.speed.toncore.pojo.JettonTransferDto;
import com.speed.toncore.withdraw.request.WithdrawRequest;

import java.math.BigDecimal;

public interface OnChainTxService {

	long getLatestLt(String tokenAddress);

	void createOnChainDebitTx(String transactionHash, WithdrawRequest withdrawRequest, String identifier);

	void createConfirmedCreditOnChainTx(JettonTransferDto transfer, int decimals);

	void updateConfirmedDebitOnChainTx(JettonTransferDto transfer, int decimals, BigDecimal fees);

	void updateLatestLogicalTime(String id, Long logicalTime);
}

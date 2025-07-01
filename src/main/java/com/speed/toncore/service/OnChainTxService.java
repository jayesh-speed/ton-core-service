package com.speed.toncore.service;

import com.speed.toncore.withdraw.request.WithdrawRequest;
import com.speed.toncore.pojo.JettonTransferDto;

public interface OnChainTxService {

	long getLatestLt(String jettonAddress);

	void createOnChainDebitTx(String transactionHash, WithdrawRequest withdrawRequest, String identifier);

	void createConfirmedCreditOnChainTx(JettonTransferDto transfer, int jettonDecimals);

	void updateConfirmedDebitOnChainTx(JettonTransferDto transfer, int tokenDecimals);

	void updateLatestLogicalTime(String id, Long logicalTime);
}

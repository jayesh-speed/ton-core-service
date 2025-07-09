package com.speed.toncore.sweep.service;

import com.speed.toncore.pojo.JettonTransferDto;
import com.speed.toncore.sweep.request.SweepRequest;
import com.speed.toncore.sweep.response.SweepResponse;

public interface SweepService {

	void updateConfirmedSweepOnChainTx(JettonTransferDto transfer, Integer chainId);

	String initiateSweepOnChainTx(SweepRequest sweepRequest, String id);

	SweepResponse createSweepOnChainTx(SweepRequest sweepRequest, String id);
}

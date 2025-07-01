package com.speed.toncore.withdraw.service;

import com.speed.toncore.withdraw.request.WithdrawRequest;
import com.speed.toncore.withdraw.response.WithdrawResponse;

public interface WithdrawService {

	WithdrawResponse transferJetton(WithdrawRequest withdrawRequest);

	void updateLatestLogicalTime(String id, Long logicalTime);
}

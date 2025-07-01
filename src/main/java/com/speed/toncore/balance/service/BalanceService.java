package com.speed.toncore.balance.service;

import com.speed.toncore.accounts.response.BalanceResponse;

public interface BalanceService {

	BalanceResponse getTonBalance(String address);

	BalanceResponse getJettonBalance(String jettonMasterAddress, String ownerAddress);
}

package com.speed.toncore.balance.service.impl;

import com.speed.toncore.accounts.response.BalanceResponse;
import com.speed.toncore.balance.service.BalanceService;
import com.speed.toncore.jettons.response.TonJettonResponse;
import com.speed.toncore.jettons.service.TonJettonService;
import com.speed.toncore.ton.TonCoreService;
import com.speed.toncore.util.TonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BalanceServiceImpl implements BalanceService {

	private final TonCoreService tonCoreService;
	private final TonJettonService tonJettonService;

	@Override
	public BalanceResponse getTonBalance(String address) {
		address = TonUtils.toRawAddress(address);
		BigDecimal balance = tonCoreService.fetchTonBalance(address);
		return BalanceResponse.builder().balance(balance).build();
	}

	@Override
	public BalanceResponse getJettonBalance(String jettonMasterAddress, String address) {
		jettonMasterAddress = TonUtils.toRawAddress(jettonMasterAddress);
		address = TonUtils.toRawAddress(address);
		TonJettonResponse tonJettonByAddress = tonJettonService.getTonJettonByAddress(jettonMasterAddress);
		BigDecimal balance = tonCoreService.fetchJettonBalance(jettonMasterAddress, address, tonJettonByAddress.getDecimals());
		return BalanceResponse.builder().balance(balance).build();
	}
}

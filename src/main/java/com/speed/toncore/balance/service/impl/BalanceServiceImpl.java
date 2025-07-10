package com.speed.toncore.balance.service.impl;

import com.speed.javacommon.exceptions.BadRequestException;
import com.speed.toncore.accounts.response.BalanceResponse;
import com.speed.toncore.balance.service.BalanceService;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.jettons.response.TonJettonResponse;
import com.speed.toncore.jettons.service.TonJettonService;
import com.speed.toncore.ton.TonCoreService;
import com.speed.toncore.util.TonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BalanceServiceImpl implements BalanceService {

	private final TonCoreService tonCoreService;
	private final TonJettonService tonJettonService;

	@Override
	public BalanceResponse getTonBalance(String address) {
		address = TonUtil.toRawAddress(address);
		return BalanceResponse.builder().balance(tonCoreService.fetchTonBalance(address)).build();
	}

	@Override
	public BalanceResponse getJettonBalance(String jettonMasterAddress, String ownerAddress) {
		jettonMasterAddress = TonUtil.toRawAddress(jettonMasterAddress);
		ownerAddress = TonUtil.toRawAddress(ownerAddress);
		TonJettonResponse jetton = tonJettonService.getTonJettonByAddress(jettonMasterAddress);
		if (Objects.isNull(jetton)) {
			throw new BadRequestException(Errors.JETTON_ADDRESS_NOT_SUPPORTED, null, null);
		}
		BigDecimal balance = tonCoreService.fetchJettonBalance(jettonMasterAddress, ownerAddress, jetton.getDecimals());
		return BalanceResponse.builder().balance(balance).build();
	}
}

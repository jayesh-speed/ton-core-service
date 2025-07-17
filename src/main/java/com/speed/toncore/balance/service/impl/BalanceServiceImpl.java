package com.speed.toncore.balance.service.impl;

import com.speed.javacommon.exceptions.BadRequestException;
import com.speed.toncore.accounts.response.BalanceResponse;
import com.speed.toncore.balance.service.BalanceService;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.tokens.response.TonTokenResponse;
import com.speed.toncore.tokens.service.TonTokenService;
import com.speed.toncore.ton.TonCoreService;
import com.speed.toncore.util.TonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BalanceServiceImpl implements BalanceService {

	private final TonCoreService tonCoreService;
	private final TonTokenService tonTokenService;

	@Override
	public BalanceResponse getTonBalance(String address) {
		address = TonUtil.toRawAddress(address);
		return BalanceResponse.builder().balance(tonCoreService.fetchTonBalance(address)).build();
	}

	@Override
	public BalanceResponse getTokenBalance(String tokenAddress, String ownerAddress) {
		tokenAddress = TonUtil.toRawAddress(tokenAddress);
		ownerAddress = TonUtil.toRawAddress(ownerAddress);
		TonTokenResponse token = tonTokenService.getTonTokenByAddress(tokenAddress);
		if (Objects.isNull(token)) {
			throw new BadRequestException(Errors.TOKEN_ADDRESS_NOT_SUPPORTED, null, null);
		}
		return BalanceResponse.builder().balance(tonCoreService.fetchTokenBalance(tokenAddress, ownerAddress, token.getDecimals())).build();
	}
}

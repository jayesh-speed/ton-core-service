package com.speed.toncore.accounts.service;

import com.speed.toncore.accounts.request.TonWalletRequest;
import com.speed.toncore.accounts.response.TonAccountResponse;
import com.speed.toncore.domain.model.TonUsedWalletAddress;

import java.util.List;
import java.util.Set;

public interface TonWalletService {

	void createPoolOfTonWalletAddresses(TonWalletRequest tonWalletRequest);

	TonUsedWalletAddress getNewWalletAddress();

	void checkAddressAvailabilityAndCreate();

	void removeUsedTonWalletAddress(String address);

	Set<String> fetchReceiveAddresses(Integer chainId);

	Set<String> fetchSendAddresses(Integer chainId);
}

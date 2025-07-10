package com.speed.toncore.accounts.service;

import com.speed.toncore.accounts.request.TonWalletRequest;
import com.speed.toncore.accounts.response.TonAccountResponse;

import java.util.Set;

public interface TonWalletService {

	void createPoolOfTonWalletAddresses(TonWalletRequest tonWalletRequest);

	TonAccountResponse getNewWalletAddress();

	void checkAddressAvailabilityAndCreate();

	void removeUsedTonWalletAddress(String address);

	Set<String> fetchReceiveAddresses(Integer chainId);

	void clearReceiveAddressesCache(Integer chainId);

	Set<String> fetchSendAddresses(Integer chainId);

	void clearSendAddressesCache(Integer chainId);
}

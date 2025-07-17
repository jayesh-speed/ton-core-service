package com.speed.toncore.accounts.service;

import com.speed.toncore.accounts.request.TonAddressRequest;
import com.speed.toncore.accounts.response.TonAccountResponse;

import java.util.Set;

public interface TonAddressService {

	void createPoolOfTonAddresses(TonAddressRequest tonAddressRequest);

	TonAccountResponse getNewTonAddress();

	void checkAddressAvailabilityAndCreate();

	void removeUsedTonAddress(String address);

	Set<String> fetchReceiveAddresses(Integer chainId);

	void clearReceiveAddressesCache(Integer chainId);

	Set<String> fetchSendAddresses(Integer chainId);

	void clearSendAddressesCache(Integer chainId);
}

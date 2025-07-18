package com.speed.toncore.accounts.service;

import com.speed.toncore.accounts.response.DeployedAccountResponse;
import com.speed.toncore.accounts.response.TonAccountResponse;
import com.speed.toncore.domain.model.TonMainAccount;
import com.speed.toncore.enums.BalancePreference;

import java.util.List;

public interface TonMainAccountService {

	void updateMainAccountBalance(String address, String tokenAddress, int decimals);

	TonAccountResponse createMainAccount(String tokenAddress);

	void deleteMainAccount(String address);

	void updateMainAccountContractAddress(String address, String tokenAddress);

	DeployedAccountResponse deployMainAccount(String address);

	List<TonAccountResponse> getMainAccounts(String tokenAddress);

	TonMainAccount getMainAccountInternal(String tokenAddress, BalancePreference order);

	void updateAccountStatusInActive(String address);
}

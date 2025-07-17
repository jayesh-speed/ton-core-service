package com.speed.toncore.accounts.service;

import com.speed.toncore.accounts.response.DeployedAccountResponse;
import com.speed.toncore.accounts.response.TonAccountResponse;
import com.speed.toncore.domain.model.TonMainAccount;

import java.util.List;

public interface TonMainAccountService {

	TonAccountResponse createMainAccount(String tokenAddress);

	void deleteMainAccount(String address);

	void addMainAccountContractAddress(String address);

	DeployedAccountResponse deployMainAccount(String address);

	List<TonAccountResponse> getMainAccounts(String tokenAddress);

	List<TonMainAccount> getMainAccountInternal(String tokenAddress);

}

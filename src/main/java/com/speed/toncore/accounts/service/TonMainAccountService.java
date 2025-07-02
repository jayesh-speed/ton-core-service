package com.speed.toncore.accounts.service;

import com.speed.toncore.accounts.response.DeployedAccountResponse;
import com.speed.toncore.accounts.response.TonAccountResponse;
import com.speed.toncore.domain.model.TonMainAccount;

import java.util.List;

public interface TonMainAccountService {

	TonAccountResponse createMainAccount(String jettonMasterAddress);

	void deleteMainAccount(String address);

	void addMainAccountJettonWallet(String address);

	DeployedAccountResponse deployMainAccount(String address);

	List<TonAccountResponse> getMainAccounts(String jettonMasterAddress);

	List<TonMainAccount> getMainAccountDetail(String address);
}

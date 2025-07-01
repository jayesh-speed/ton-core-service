package com.speed.toncore.accounts.service;

import com.speed.toncore.accounts.response.DeployedAccountResponse;
import com.speed.toncore.accounts.response.TonAccountResponse;
import com.speed.toncore.domain.model.TonMainAccount;

public interface TonMainAccountService {

	TonAccountResponse createMainAccount(String jettonMasterAddress);

	void deleteMainAccount(String address);

	DeployedAccountResponse deployMainAccount(String address);

	TonAccountResponse getMainAccount(String jettonAddress);

	TonMainAccount getMainAccountDetail(String address);
}

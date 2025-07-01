package com.speed.toncore.accounts.service;

import com.speed.toncore.accounts.response.DeployedAccountResponse;
import com.speed.toncore.accounts.response.TonAccountResponse;
import com.speed.toncore.domain.model.TonFeeAccount;

import java.util.List;

public interface TonFeeAccountService {

	TonAccountResponse createFeeAccount();

	void deleteFeeAccount(String address);

	DeployedAccountResponse deployFeeAccount(String address);

	List<TonAccountResponse> getFeeAccounts();

	List<TonFeeAccount> getTonFeeAccounts();
}

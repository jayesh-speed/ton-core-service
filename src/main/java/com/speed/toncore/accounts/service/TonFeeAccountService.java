package com.speed.toncore.accounts.service;

import com.speed.toncore.accounts.response.DeployedAccountResponse;
import com.speed.toncore.accounts.response.TonAccountResponse;
import com.speed.toncore.domain.model.TonFeeAccount;

public interface TonFeeAccountService {

	TonAccountResponse createFeeAccount();

	void deleteFeeAccount(String address);

	DeployedAccountResponse deployFeeAccount(String address);

	TonAccountResponse getTonBalance(String address);

	TonAccountResponse updateFeeAccountLocalBalance(String address);

	TonAccountResponse getFeeAccount();

	TonFeeAccount getTonFeeAccount();
}

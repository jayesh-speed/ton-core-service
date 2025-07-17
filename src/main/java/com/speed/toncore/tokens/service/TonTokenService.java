package com.speed.toncore.tokens.service;

import com.speed.toncore.tokens.request.TonTokenRequest;
import com.speed.toncore.tokens.response.TonTokenResponse;

import java.util.List;

public interface TonTokenService {

	TonTokenResponse addNewTonToken(TonTokenRequest tonTokenRequest);

	void deleteTonToken(String address);

	List<TonTokenResponse> getAllTokens();

	TonTokenResponse getTonTokenBySymbol(String tokenSymbol);

	TonTokenResponse getTonTokenByAddress(String tokenAddress);
}

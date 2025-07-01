package com.speed.toncore.jettons.service;

import com.speed.toncore.jettons.request.TonJettonRequest;
import com.speed.toncore.jettons.response.TonJettonResponse;

import java.util.List;

public interface TonJettonService {

	TonJettonResponse addNewTonJetton(TonJettonRequest tonJettonRequest);

	void deleteTonJetton(String address);

	List<TonJettonResponse> getAllJettons();

	TonJettonResponse getTonJettonBySymbol(String jettonSymbol);

	TonJettonResponse getTonJettonByAddress(String jettonAddress);
}

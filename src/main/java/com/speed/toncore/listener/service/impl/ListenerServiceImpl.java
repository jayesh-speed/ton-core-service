package com.speed.toncore.listener.service.impl;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.speed.javacommon.exceptions.BadRequestException;
import com.speed.javacommon.util.DateTimeUtil;
import com.speed.toncore.accounts.service.TonAddressService;
import com.speed.toncore.listener.TokenTransferFilter;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Endpoints;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.domain.model.QTonListener;
import com.speed.toncore.domain.model.TonListener;
import com.speed.toncore.enums.TonListenerStatus;
import com.speed.toncore.enums.TonTransactionType;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import com.speed.toncore.listener.service.TonListenerService;
import com.speed.toncore.pojo.JettonTransferDto;
import com.speed.toncore.repository.TonListenerRepository;
import com.speed.toncore.service.OnChainTxService;
import com.speed.toncore.tokens.response.TonTokenResponse;
import com.speed.toncore.tokens.service.TonTokenService;
import com.speed.toncore.ton.TonNode;
import com.speed.toncore.ton.TonNodePool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListenerServiceImpl implements TonListenerService {

	private static final QTonListener qTonListener = QTonListener.tonListener;

	private final TonListenerRepository tonListenerRepository;
	private final TonListenerHelper tonListenerHelper;
	private final TonTokenService tonTokenService;
	private final OnChainTxService onChainTxService;
	private final TonNodePool tonNodePool;
	private final Map<TonListener, TokenTransferFilter> tonChainListenerMap = new ConcurrentHashMap<>();
	private final TonAddressService tonAddressService;
	private final OkHttpClient okHttpClient;

	@Override
	public void bootUpTonListeners(boolean initialBootUp) {
		TonListener idleListener;
		if (initialBootUp) {
			idleListener = tonListenerHelper.fetchListenerAndUpdateToRunning();
		} else {
			idleListener = tonListenerHelper.fetchIdleListenerAndUpdateToRunning();
		}
		if (Objects.isNull(idleListener)) {
			return;
		}
		stopAndDisposeListener(idleListener);
		subscribeListener(idleListener);
	}

	@Override
	public void subscribeListener(TonListener idleListener) {
		try {
			List<String> tokenAddresses = tonTokenService.getAllTokens().stream().map(TonTokenResponse::getTokenAddress).toList();
			TonNode tonNode = tonNodePool.getTonNodeByChainId();
			TokenTransferFilter filter = runAndGetTokenListener(tokenAddresses, tonNode, idleListener);
			tonChainListenerMap.put(idleListener, filter);
		} catch (Exception ex) {
			LOG.error(Errors.ERROR_SUBSCRIBE_TOKEN_LISTENER, ex);
			stopAndDisposeListener(idleListener);
			tonListenerHelper.updateListenerToIdle(idleListener);
		}
	}

	@Override
	public void stopAndDisposeListener(TonListener listener) {
		if (tonChainListenerMap.containsKey(listener)) {
			tonChainListenerMap.remove(listener).stop();
		}
	}

	private TokenTransferFilter runAndGetTokenListener(List<String> tokenAddresses, TonNode tonNode, TonListener listener) {
		List<MutablePair<String, Long>> tokenLTimePair = new ArrayList<>();
		tokenAddresses.forEach(tokenAddress -> tokenLTimePair.add(MutablePair.of(tokenAddress, onChainTxService.getLatestLt(tokenAddress))));
		TokenTransferFilter tokenFilter = TokenTransferFilter.builder()
				.httpClient(okHttpClient)
				.pollingInterval(tonNode.isMainNet() ? Constants.MAIN_NET_POLLING_INTERVAL : Constants.TEST_NET_POLLING_INTERVAL)
				.tokenAddresses(tokenLTimePair)
				.apiUrl(tonNode.getListenerBaseUrl() + Endpoints.TonIndexer.GET_TOKEN_TRANSFERS)
				.apiKey(tonNode.getListenerApiKey())
				.build();
		tokenFilter.start().subscribe(transfer -> verifyAndUpdateOnChainTx(transfer, tonNode.getChainId()), error -> {
			if (tonChainListenerMap.containsKey(listener)) {
				tonChainListenerMap.get(listener).stop();
			}
			tonListenerHelper.updateListenerToIdle(listener);
			LOG.error(String.format(Errors.ERROR_SUBSCRIBING_ONCHAIN_TRANSACTION, listener.getId()), error);
		});
		return tokenFilter;
	}

	private void verifyAndUpdateOnChainTx(JettonTransferDto transfer, Integer chainId) {
		Set<String> toAddresses = tonAddressService.fetchReceiveAddresses(chainId);
		if (toAddresses.contains(transfer.getDestination())) {
			tonListenerHelper.updateOnChainTransaction(transfer, TonTransactionType.RECEIVE.name(), chainId);
		}
		Set<String> fromAddresses = tonAddressService.fetchSendAddresses(chainId);
		if (fromAddresses.contains(transfer.getSource())) {
			tonListenerHelper.updateOnChainTransaction(transfer, TonTransactionType.SEND.name(), chainId);
		}
		if (fromAddresses.contains(transfer.getDestination())) {
			if (toAddresses.contains(transfer.getSource())) {
				tonListenerHelper.updateOnChainSweepTransaction(transfer, chainId);
				return;
			}
			tonListenerHelper.updateTokenContractAddress(transfer.getDestination(), transfer.getJettonMaster(), chainId);
		}
	}

	@Override
	public void createTonListener() {
		TonListener tonListener = new TonListener();
		tonListener.setChainId(ExecutionContextUtil.getContext().getChainId());
		tonListener.setMainNet(ExecutionContextUtil.getContext().isMainNet());
		tonListener.setStatus(TonListenerStatus.IDLE.name());
		tonListenerRepository.save(tonListener);
	}

	@Override
	public void deleteListenerById(String id) {
		tonListenerRepository.deleteById(id);
	}

	@Override
	public List<TonListener> fetchAllListeners() {
		return tonListenerRepository.findAll();
	}

	@Override
	public void updateListenerStatus(TonListener listener, String status) {
		Predicate queryPredicate = qTonListener.id.eq(listener.getId());
		Long currentTime = DateTimeUtil.currentEpochMilliSecondsUTC();
		Map<Path<?>, Object> fieldWithValue = HashMap.newHashMap(2);
		fieldWithValue.put(qTonListener.status, status);
		fieldWithValue.put(qTonListener.modified, currentTime);
		tonListenerRepository.updateFields(queryPredicate, qTonListener, fieldWithValue);
	}

	@Override
	public TonListener getListenerById(String id) {
		Optional<TonListener> tonListener = tonListenerRepository.findById(id);
		return tonListener.orElseThrow(() -> new BadRequestException(String.format(Errors.LISTENER_NOT_FOUND, id), null, null));
	}

	@Override
	public TonListener getListener() {
		return tonListenerRepository.findByChainId(ExecutionContextUtil.getContext().getChainId());
	}
}

package com.speed.toncore.listener.service.impl;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.speed.javacommon.exceptions.BadRequestException;
import com.speed.javacommon.util.DateTimeUtil;
import com.speed.toncore.accounts.service.TonWalletService;
import com.speed.toncore.chainstack.JettonTransferFilter;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Endpoints;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.domain.model.QTonListener;
import com.speed.toncore.domain.model.TonListener;
import com.speed.toncore.enums.TonListenerStatus;
import com.speed.toncore.enums.TonTransactionType;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import com.speed.toncore.jettons.response.TonJettonResponse;
import com.speed.toncore.jettons.service.TonJettonService;
import com.speed.toncore.listener.service.TonListenerService;
import com.speed.toncore.pojo.JettonTransferDto;
import com.speed.toncore.repository.TonListenerRepository;
import com.speed.toncore.service.OnChainTxService;
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
	private final TonJettonService tonJettonService;
	private final OnChainTxService onChainTxService;
	private final TonNodePool tonNodePool;
	private final Map<TonListener, JettonTransferFilter> tonChainListenerMap = new ConcurrentHashMap<>();
	private final TonWalletService tonWalletService;
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
			List<String> jettonMasterAddresses = tonJettonService.getAllJettons().stream().map(TonJettonResponse::getJettonMasterAddress).toList();
			TonNode tonNode = tonNodePool.getTonNodeByChainId();
			JettonTransferFilter filter = runAndGetJettonListener(jettonMasterAddresses, tonNode, idleListener);
			tonChainListenerMap.put(idleListener, filter);
		} catch (Exception ex) {
			LOG.error(Errors.ERROR_SUBSCRIBE_JETTON_LISTENER, ex);
			stopAndDisposeListener(idleListener);
			tonListenerHelper.updateListenerToIdle(idleListener);
		}
	}

	@Override
	public void stopAndDisposeListener(TonListener listener) {
		if (Objects.nonNull(tonChainListenerMap.get(listener))) {
			tonChainListenerMap.get(listener).stop();
			tonChainListenerMap.remove(listener);
		}
	}

	private JettonTransferFilter runAndGetJettonListener(List<String> jettonMasterAddresses, TonNode tonNode, TonListener listener) {
		List<MutablePair<String, Long>> jettonMasters = new ArrayList<>();
		jettonMasterAddresses.forEach(
				jettonMasterAddress -> jettonMasters.add(MutablePair.of(jettonMasterAddress, onChainTxService.getLatestLt(jettonMasterAddress))));
		JettonTransferFilter jettonFilter = JettonTransferFilter.builder()
				.httpClient(okHttpClient)
				.pollingInterval(tonNode.isMainNet() ? Constants.MAIN_NET_POLLING_INTERVAL : Constants.TEST_NET_POLLING_INTERVAL)
				.jettonMasters(jettonMasters)
				.apiUrl(tonNode.getListenerBaseUrl() + Endpoints.TonIndexer.GET_JETTON_TRANSFERS)
				.apiKey(tonNode.getListenerApiKey())
				.build();
		jettonFilter.start().subscribe(transfer -> verifyAndUpdateOnChainTx(transfer, tonNode.getChainId()), error -> {
			if (Objects.nonNull(tonChainListenerMap.get(listener))) {
				JettonTransferFilter filter = tonChainListenerMap.get(listener);
				filter.stop();
			}
			tonListenerHelper.updateListenerToIdle(listener);
			LOG.error(String.format(Errors.ERROR_SUBSCRIBING_ONCHAIN_TRANSACTION, listener.getId()), error);
		});
		return jettonFilter;
	}

	private void verifyAndUpdateOnChainTx(JettonTransferDto transfer, Integer chainId) {
		Set<String> toAddresses = tonWalletService.fetchReceiveAddresses(chainId);
		if (toAddresses.contains(transfer.getDestination())) {
			tonListenerHelper.updateOnChainTransaction(transfer, TonTransactionType.RECEIVE.name(), chainId);
		}
		Set<String> fromAddresses = tonWalletService.fetchSendAddresses(chainId);
		if (fromAddresses.contains(transfer.getSource())) {
			tonListenerHelper.updateOnChainTransaction(transfer, TonTransactionType.SEND.name(), chainId);
		}
		if (fromAddresses.contains(transfer.getDestination())) {
			tonListenerHelper.updateOnChainSweepTransaction(transfer, chainId);
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
	public long updateListenerStatus(TonListener listener, String status) {
		Predicate queryPredicate = qTonListener.id.eq(listener.getId());
		Long currentTime = DateTimeUtil.currentEpochMilliSecondsUTC();
		Map<Path<?>, Object> fieldWithValue = HashMap.newHashMap(2);
		fieldWithValue.put(qTonListener.status, status);
		fieldWithValue.put(qTonListener.modified, currentTime);
		return tonListenerRepository.updateFields(queryPredicate, qTonListener, fieldWithValue);
	}

	@Override
	public TonListener getListenerById(String id) {
		Optional<TonListener> tonListener = tonListenerRepository.findById(id);
		return tonListener.orElseThrow(() -> new BadRequestException(String.format(Errors.LISTENER_NOT_FOUND, id), null, null));
	}

	@Override
	public TonListener getListener() {
		Predicate queryPredicate = qTonListener.chainId.eq(ExecutionContextUtil.getContext().getChainId());
		return tonListenerRepository.findAndProjectUnique(queryPredicate, qTonListener, qTonListener.id, qTonListener.chainId, qTonListener.mainNet,
				qTonListener.status, qTonListener.created, qTonListener.modified);
	}
}

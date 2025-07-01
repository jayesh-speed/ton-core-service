package com.speed.toncore.listener.service.impl;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.speed.javacommon.util.DateTimeUtil;
import com.speed.javacommon.util.RequestIdGenerator;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.domain.model.QTonListener;
import com.speed.toncore.domain.model.TonListener;
import com.speed.toncore.enums.TonListenerStatus;
import com.speed.toncore.enums.TonTransactionType;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import com.speed.toncore.jettons.response.TonJettonResponse;
import com.speed.toncore.jettons.service.TonJettonService;
import com.speed.toncore.pojo.JettonTransferDto;
import com.speed.toncore.repository.TonListenerRepository;
import com.speed.toncore.service.OnChainTxService;
import com.speed.toncore.service.WithdrawProcessHelper;
import com.speed.toncore.sweep.request.SweepRequest;
import com.speed.toncore.sweep.service.SweepService;
import com.speed.toncore.util.LogMessages;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class TonListenerHelper {

	private static final QTonListener qTonListener = QTonListener.tonListener;
	private final TonListenerRepository listenerRepository;
	private final TonJettonService tonJettonService;
	private final OnChainTxService onChainTxService;
	private final WithdrawProcessHelper withdrawProcessHelper;
	private final SweepService sweepService;

	@Transactional
	public TonListener fetchListenerAndUpdateToRunning() {
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
		TonListener listener = listenerRepository.findByChainId(chainId);
		if (Objects.isNull(listener)) {
			LOG.warn(LogMessages.Warn.LISTENER_NOT_FOUND, chainId);
			return null;
		}
		return updateListenerToRunning(listener, qTonListener.chainId.eq(chainId));
	}

	@Transactional
	public TonListener fetchIdleListenerAndUpdateToRunning() {
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
		TonListener listener = listenerRepository.findByChainIdAndStatus(chainId, TonListenerStatus.IDLE.name());
		if (Objects.isNull(listener)) {
			return null;
		}
		return updateListenerToRunning(listener, qTonListener.chainId.eq(chainId).and(qTonListener.status.eq(TonListenerStatus.IDLE.name())));
	}

	private TonListener updateListenerToRunning(TonListener listener, Predicate queryPredicate) {
		Long currentTime = DateTimeUtil.currentEpochMilliSecondsUTC();
		Map<Path<?>, Object> fieldWithValue = HashMap.newHashMap(2);
		fieldWithValue.put(qTonListener.status, TonListenerStatus.RUNNING.name());
		fieldWithValue.put(qTonListener.modified, currentTime);
		long count = listenerRepository.updateFields(queryPredicate, qTonListener, fieldWithValue);
		if (count == 0) {
			LOG.warn(LogMessages.Warn.LISTENER_ALREADY_RUNNING, listener.getId());
			return null;
		}
		return listener;
	}

	@Transactional
	public void updateListenerToIdle(TonListener listener) {
		TonListener tonListener = listenerRepository.findById(listener.getId())
				.orElseThrow(() -> new EntityNotFoundException(String.format(Errors.LISTENER_NOT_FOUND, listener.getId())));
		tonListener.setStatus(TonListenerStatus.IDLE.name());
		tonListener.setModified(DateTimeUtil.currentEpochMilliSecondsUTC());
		listenerRepository.save(tonListener);
	}

	@Async
	public void updateOnChainTransaction(JettonTransferDto transfer, String transactionType, Integer chainId) {
		LOG.info(String.format("TransactionType: %s  TransactionHash: %s ChainId: %s", transactionType, transfer.getTransactionHash(), chainId));
		setContext(chainId);
		TonJettonResponse jettonResponse = tonJettonService.getTonJettonByAddress(transfer.getJettonMaster());
		if (transactionType.equals(TonTransactionType.RECEIVE.name())) {
			updateReceivedOnChainTx(transfer, jettonResponse.getDecimals());
			return;
		}
		onChainTxService.updateConfirmedDebitOnChainTx(transfer, jettonResponse.getDecimals());
		withdrawProcessHelper.markWithdrawProcessPaid(transfer);
	}

	@Async
	public void updateOnChainSweepTransaction(JettonTransferDto transfer, Integer chainId) {
		LOG.info(String.format("TransactionHash: %s ChainId: %s", transfer.getTransactionHash(), chainId));
		setContext(chainId);
		sweepService.updateConfirmedSweepOnChainTx(transfer, chainId);
	}

	private void setContext(Integer chainId) {
		MDC.clear();

		String requestId = RequestIdGenerator.generate();
		MDC.put(Constants.REQUEST_ID, requestId);
		MDC.put(Constants.CHAIN_ID, String.valueOf(chainId));

		ExecutionContextUtil context = ExecutionContextUtil.getContext();
		context.setRequestId(requestId);
		context.setChainId(chainId);
		context.setMainNet(chainId == Constants.MAIN_NET_CHAIN_ID);
	}

	private void updateReceivedOnChainTx(JettonTransferDto transfer, int decimals) {
		onChainTxService.createConfirmedCreditOnChainTx(transfer, decimals);
		// Create Sweep
		SweepRequest sweepRequest = new SweepRequest();
		sweepRequest.setFromAddress(transfer.getDestination());
		sweepRequest.setJettonMaster(transfer.getJettonMaster());
		sweepService.initiateSweepOnChainTx(sweepRequest);
	}
}

package com.speed.toncore.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.speed.javacommon.util.DateTimeUtil;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.domain.model.QTonOnChainTx;
import com.speed.toncore.domain.model.TonOnChainTx;
import com.speed.toncore.enums.OnChainTxStatus;
import com.speed.toncore.enums.TonOnChainTxType;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import com.speed.toncore.mapper.TonOnChainPaymentMapper;
import com.speed.toncore.mapper.TonOnChainTxMapper;
import com.speed.toncore.pojo.JettonTransferDto;
import com.speed.toncore.repository.OnChainTxRepository;
import com.speed.toncore.service.OnChainTxService;
import com.speed.toncore.util.TonUtil;
import com.speed.toncore.withdraw.request.WithdrawRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class OnChainTxServiceImpl implements OnChainTxService {

	private final static QTonOnChainTx qTonOnChainTx = QTonOnChainTx.tonOnChainTx;
	private final OnChainTxRepository onChainTxRepository;

	@Override
	public long getLatestLt(String jettonMasterAddress) {
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
		BooleanBuilder predicate = new BooleanBuilder(qTonOnChainTx.chainId.eq(chainId)).and(qTonOnChainTx.logicalTime.isNotNull())
				.and(qTonOnChainTx.tokenAddress.eq(jettonMasterAddress));
		TonOnChainTx lastOnChainTransaction = onChainTxRepository.getMaxLogicalTimeByPredicate(predicate);
		if (Objects.isNull(lastOnChainTransaction)) {
			return Objects.equals(chainId, Constants.MAIN_NET_CHAIN_ID) ? 58148306000003L : 0L;
		}
		return lastOnChainTransaction.getLogicalTime();
	}

	@Override
	public void updateLatestLogicalTime(String id, Long logicalTime) {
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
		Predicate predicate = new BooleanBuilder(qTonOnChainTx.chainId.eq(chainId).and(qTonOnChainTx.id.eq(id)));
		Map<Path<?>, Object> fieldWithValue = HashMap.newHashMap(1);
		fieldWithValue.put(qTonOnChainTx.logicalTime, logicalTime);
		onChainTxRepository.updateFields(predicate, qTonOnChainTx, fieldWithValue);
	}

	@Async
	@Override
	public void createOnChainDebitTx(String transactionHash, WithdrawRequest withdrawRequest, String txReference) {
		TonOnChainTx onChainTx = TonOnChainPaymentMapper.INSTANCE.mapWithdrawReqToOnChainTx(withdrawRequest);
		onChainTx.setTransactionHash(transactionHash);
		onChainTx.setTransactionType(TonOnChainTxType.DEBIT.name());
		onChainTx.setTransactionStatus(OnChainTxStatus.PENDING.getValue());
		onChainTx.setTxReference(txReference);
		onChainTxRepository.save(onChainTx);
	}

	@Override
	public void createConfirmedCreditOnChainTx(JettonTransferDto transfer, int jettonDecimals) {
		TonOnChainTx onChainTx = TonOnChainTxMapper.INSTANCE.mapTransferToOnChainTx(transfer);
		onChainTx.setTransactionType(TonOnChainTxType.CREDIT.name());
		onChainTx.setTransactionStatus(OnChainTxStatus.CONFIRMED.getValue());
		onChainTx.setAmount(onChainTx.getAmount().divide(BigDecimal.TEN.pow(jettonDecimals), jettonDecimals, RoundingMode.HALF_DOWN));
		try {
			onChainTxRepository.save(onChainTx);
		} catch (DataIntegrityViolationException | ConstraintViolationException e) {
			LOG.warn(String.format(Errors.CREDIT_ON_CHAIN_TX_CONSTRAINT_VIOLATION, onChainTx.getFromAddress(), onChainTx.getTransactionHash()));
		}
	}

	@Override
	public void updateConfirmedDebitOnChainTx(JettonTransferDto transfer, int jettonDecimals, BigDecimal fees) {
		String txReference = TonUtil.deserializeTransactionReference(transfer.getForwardPayload());
		Predicate queryPredicate = new BooleanBuilder(qTonOnChainTx.txReference.eq(txReference));
		TonOnChainTx onChainTx = onChainTxRepository.findAndProjectUnique(queryPredicate, qTonOnChainTx, qTonOnChainTx.id, qTonOnChainTx.transactionHash,
				qTonOnChainTx.toAddress, qTonOnChainTx.tokenAddress);
		if (Objects.nonNull(onChainTx)) {
			Map<Path<?>, Object> fieldWithValue = HashMap.newHashMap(7);
			fieldWithValue.put(qTonOnChainTx.transactionHash, transfer.getTransactionHash());
			fieldWithValue.put(qTonOnChainTx.traceId, transfer.getTraceId());
			fieldWithValue.put(qTonOnChainTx.confirmationTimestamp, transfer.getTransactionNow());
			fieldWithValue.put(qTonOnChainTx.transactionStatus, OnChainTxStatus.CONFIRMED.getValue());
			fieldWithValue.put(qTonOnChainTx.transactionFee, fees);
			fieldWithValue.put(qTonOnChainTx.logicalTime, transfer.getTransactionLt());
			updateOnChainTx(onChainTx, fieldWithValue);
			return;
		}
		onChainTx = TonOnChainTxMapper.INSTANCE.mapTransferToOnChainTx(transfer);
		onChainTx.setAmount(new BigDecimal(transfer.getAmount()).divide(BigDecimal.TEN.pow(jettonDecimals), jettonDecimals, RoundingMode.HALF_DOWN));
		onChainTx.setTransactionFee(fees);
		onChainTx.setTxReference(txReference);
		onChainTx.setTransactionType(TonOnChainTxType.DEBIT.name());
		onChainTx.setTransactionStatus(OnChainTxStatus.CONFIRMED.getValue());
		try {
			onChainTxRepository.save(onChainTx);
		} catch (DataIntegrityViolationException | ConstraintViolationException e) {
			LOG.warn(String.format(Errors.DEBIT_ON_CHAIN_TX_CONSTRAINT_VIOLATION, onChainTx.getFromAddress(), onChainTx.getTransactionHash()));
		}
	}

	private void updateOnChainTx(TonOnChainTx onChainTx, Map<Path<?>, Object> fieldWithValue) {
		Long currentTime = DateTimeUtil.currentEpochMilliSecondsUTC();
		fieldWithValue.put(qTonOnChainTx.modified, currentTime);
		Predicate queryPredicate = new BooleanBuilder(qTonOnChainTx.id.eq(onChainTx.getId()));
		long updateCount = onChainTxRepository.updateFields(queryPredicate, qTonOnChainTx, fieldWithValue);
		if (updateCount == 0) {
			LOG.error(String.format(Errors.CONFIRM_ON_CHAIN_TX_UPDATE_FAIL, onChainTx.getToAddress(), onChainTx.getTokenAddress(),
					onChainTx.getTransactionHash()));
		}
	}
}

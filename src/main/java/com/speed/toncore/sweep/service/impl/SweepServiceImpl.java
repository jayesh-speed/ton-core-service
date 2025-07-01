package com.speed.toncore.sweep.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.speed.javacommon.exceptions.BadRequestException;
import com.speed.javacommon.util.DateTimeUtil;
import com.speed.javacommon.util.StringUtil;
import com.speed.toncore.accounts.service.TonFeeAccountService;
import com.speed.toncore.accounts.service.TonWalletService;
import com.speed.toncore.accounts.service.TransactionFeeService;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.domain.model.QTonSweepTx;
import com.speed.toncore.domain.model.QTonUsedWalletAddress;
import com.speed.toncore.domain.model.TonFeeAccount;
import com.speed.toncore.domain.model.TonSweepTx;
import com.speed.toncore.domain.model.TonUsedWalletAddress;
import com.speed.toncore.enums.OnChainTxStatus;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import com.speed.toncore.jettons.response.TonJettonResponse;
import com.speed.toncore.jettons.service.TonJettonService;
import com.speed.toncore.pojo.JettonTransferDto;
import com.speed.toncore.repository.TonSweepTxRepository;
import com.speed.toncore.repository.TonUsedWalletAddressRepository;
import com.speed.toncore.sweep.request.SweepRequest;
import com.speed.toncore.sweep.response.SweepResponse;
import com.speed.toncore.sweep.service.SweepService;
import com.speed.toncore.ton.TonCoreService;
import com.speed.toncore.ton.TonCoreServiceHelper;
import com.speed.toncore.ton.TonNode;
import com.speed.toncore.ton.TonNodePool;
import com.speed.toncore.util.TonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class SweepServiceImpl implements SweepService {

	private static final QTonSweepTx qTonSweepTx = QTonSweepTx.tonSweepTx;
	private static final QTonUsedWalletAddress qTonUsedWalletAddress = QTonUsedWalletAddress.tonUsedWalletAddress;
	private final TonSweepTxRepository tonSweepTxRepository;
	private final TonUsedWalletAddressRepository tonUsedWalletAddressRepository;
	private final TonJettonService tonJettonService;
	private final TonFeeAccountService tonFeeAccountService;
	private final TonWalletService tonWalletService;
	private final TonNodePool tonNodePool;
	private final TonCoreService tonCoreService;
	private final TonCoreServiceHelper tonCoreServiceHelper;
	private final TransactionFeeService transactionFeeService;

	@Override
	public void updateConfirmedSweepOnChainTx(JettonTransferDto transfer, Integer chainId) {
		String txReference = TonUtils.deserializeTransactionReference(transfer.getForwardPayload());
		Predicate queryPredicate = qTonSweepTx.chainId.eq(chainId).and(qTonSweepTx.txReference.eq(txReference));
		TonSweepTx sweepTx = tonSweepTxRepository.findAndProjectUnique(queryPredicate, qTonSweepTx, qTonSweepTx.id);
		TonJettonResponse jettonResponse = tonJettonService.getTonJettonByAddress(transfer.getJettonMaster());
		BigDecimal transactionFee = transactionFeeService.getSweepTransactionFee(transfer.getTraceId());
		if (Objects.nonNull(sweepTx)) {
			Map<Path<?>, Object> fieldWithValue = HashMap.newHashMap(9);
			fieldWithValue.put(qTonSweepTx.transactionHash, transfer.getTransactionHash());
			fieldWithValue.put(qTonSweepTx.traceId, transfer.getTraceId());
			fieldWithValue.put(qTonSweepTx.amount,
					new BigDecimal(transfer.getAmount()).divide(BigDecimal.TEN.pow(jettonResponse.getDecimals()), jettonResponse.getDecimals(),
							RoundingMode.HALF_DOWN));
			fieldWithValue.put(qTonSweepTx.transactionFee, transactionFee);
			fieldWithValue.put(qTonSweepTx.confirmationTimestamp, transfer.getTransactionNow());
			fieldWithValue.put(qTonSweepTx.transactionStatus, OnChainTxStatus.CONFIRMED.getValue());
			fieldWithValue.put(qTonSweepTx.logicalTime, transfer.getTransactionLt());
			updateOnChainTx(sweepTx, fieldWithValue);
		}
	}

	@Override
	public String initiateSweepOnChainTx(SweepRequest sweepRequest) {
		TonNode tonNode = tonNodePool.getTonNodeByChainId();
		TonFeeAccount feeAccount = tonFeeAccountService.getTonFeeAccounts().getFirst();
		String mainAccountAddress = tonWalletService.fetchSendAddresses(tonNode.getChainId()).stream().toList().getFirst();
		TonUsedWalletAddress address = tonUsedWalletAddressRepository.findAndProjectUnique(
				qTonUsedWalletAddress.chainId.eq(tonNode.getChainId()).and(qTonUsedWalletAddress.address.eq(sweepRequest.getFromAddress())),
				qTonUsedWalletAddress, qTonUsedWalletAddress.address, qTonUsedWalletAddress.secretKey);
		String txReference = TonUtils.generateSweepTransactionReference();
		String hash = tonCoreService.transferJettonToMainAccount(address.getAddress(), address.getSecretKey(), sweepRequest.getJettonMasterAddress(),
				mainAccountAddress, feeAccount.getSecretKey(), txReference);
		if (StringUtil.nullOrEmpty(hash)) {
			return "";
		}
		TonSweepTx tonSweepTx = TonSweepTx.builder()
				.toAddress(mainAccountAddress)
				.fromAddress(sweepRequest.getFromAddress())
				.jettonMasterAddress(sweepRequest.getJettonMasterAddress())
				.transactionHash(hash)
				.transactionStatus(OnChainTxStatus.PENDING.getValue())
				.txReference(txReference)
				.mainNet(tonNode.isMainNet())
				.chainId(tonNode.getChainId())
				.timestamp(System.currentTimeMillis())
				.transactionDate(DateTimeUtil.convertToLocalDateTime(Date.from(Instant.now())))
				.build();
		tonSweepTxRepository.save(tonSweepTx);
		return hash;
	}

	@Override
	public SweepResponse createSweepOnChainTx(SweepRequest sweepRequest) {
		TonJettonResponse jetton = tonJettonService.getTonJettonByAddress(sweepRequest.getJettonMasterAddress());
		if (Objects.isNull(jetton)) {
			throw new BadRequestException(Errors.JETTON_ADDRESS_NOT_SUPPORTED, null, null);
		}
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
		if (!tonUsedWalletAddressRepository.exists(
				qTonUsedWalletAddress.chainId.eq(chainId).and(qTonUsedWalletAddress.address.eq(sweepRequest.getFromAddress())))) {
			throw new BadRequestException(String.format(Errors.NOT_ELIGIBLE_FOR_SWEEP, sweepRequest.getFromAddress()), null, null);
		}
		String hash = initiateSweepOnChainTx(sweepRequest);
		return SweepResponse.builder().transactionHash(hash).build();
	}

	private void updateOnChainTx(TonSweepTx sweepTx, Map<Path<?>, Object> fieldWithValue) {
		Long currentTime = DateTimeUtil.currentEpochMilliSecondsUTC();
		fieldWithValue.put(qTonSweepTx.modified, currentTime);
		Predicate queryPredicate = new BooleanBuilder(qTonSweepTx.id.eq(sweepTx.getId()));
		long updateCount = tonSweepTxRepository.updateFields(queryPredicate, qTonSweepTx, fieldWithValue);
		if (updateCount < 0) {
			LOG.error(String.format(Errors.CONFIRM_ON_CHAIN_TX_UPDATE_FAIL, sweepTx.getToAddress(), sweepTx.getJettonMasterAddress(),
					sweepTx.getTransactionHash()));
		}
	}
}

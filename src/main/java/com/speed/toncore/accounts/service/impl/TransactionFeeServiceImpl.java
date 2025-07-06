package com.speed.toncore.accounts.service.impl;

import com.speed.javacommon.util.CollectionUtil;
import com.speed.javacommon.util.StringUtil;
import com.speed.toncore.accounts.request.FeeEstimationRequest;
import com.speed.toncore.accounts.response.EstimateFeeResponse;
import com.speed.toncore.accounts.service.TonMainAccountService;
import com.speed.toncore.accounts.service.TransactionFeeService;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.domain.model.TonMainAccount;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import com.speed.toncore.pojo.JettonWalletDto;
import com.speed.toncore.pojo.TraceDto;
import com.speed.toncore.schedular.ConfigParam;
import com.speed.toncore.schedular.TonConfigParam;
import com.speed.toncore.ton.TonCoreServiceHelper;
import com.speed.toncore.util.LogMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ton.ton4j.cell.CellBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionFeeServiceImpl implements TransactionFeeService {

	private static final int BIT16 = 65536;   // 2^16, used for fee calculations
	private final TonCoreServiceHelper tonCoreServiceHelper;
	private final TonMainAccountService tonMainAccountService;

	private TraceDto waitForTraceReady(String traceId) throws InterruptedException {
		for (int attempt = 1; attempt <= 5; attempt++) {
			TraceDto traceDto = tonCoreServiceHelper.getTraceByTraceId(traceId);
			if (traceDto != null && traceDto.getTraces() != null && !traceDto.getTraces().isEmpty()) {
				return traceDto;
			}
			LOG.info(String.format(LogMessages.Info.WAITING_FOR_TRACE_UPDATE, traceId));
			Thread.sleep(1000);
		}
		throw new IllegalStateException();
	}

	@Override
	public BigDecimal getJettonTransactionFee(String traceId) {
		try {
			TraceDto traceDto = waitForTraceReady(traceId);

			TraceDto.Trace trace = traceDto.getTraces().getFirst();
			List<String> txOrder = trace.getTransactionsOrder();
			Map<String, TraceDto.Trace.Transaction> txMap = trace.getTransactions();

			TraceDto.Trace.Transaction firstTx = txMap.get(txOrder.getFirst());
			String firstTxFee = firstTx.getTotalFees();
			String firstTxFwdFee = firstTx.getOutMsgs().getFirst().getFwdFee();

			TraceDto.Trace.Transaction secondTx = txMap.get(txOrder.get(1));
			String secondTxFee = secondTx.getTotalFees();
			TraceDto.Trace.Transaction.Message secondOutMsg = secondTx.getOutMsgs().getFirst();
			String secondTxFwdFee = secondOutMsg.getFwdFee();
			String forwardedTonAmount = secondOutMsg.getValue();

			TraceDto.Trace.Transaction finalTx = txMap.get(txOrder.getLast());
			if ("1".equalsIgnoreCase(finalTx.getInMsg().getValue()) && txOrder.size() > 4) {
				finalTx = txMap.get(txOrder.get(txOrder.size() - 2));
			}

			String finalTxFee = finalTx.getTotalFees();
			String excessTonAmount = finalTx.getInMsg().getValue();

			return new BigDecimal(firstTxFee).add(new BigDecimal(firstTxFwdFee))
					.add(new BigDecimal(secondTxFee))
					.add(new BigDecimal(secondTxFwdFee))
					.add(new BigDecimal(finalTxFee))
					.add(new BigDecimal(forwardedTonAmount).subtract(new BigDecimal(excessTonAmount)));

		} catch (Exception e) {
			LOG.error(String.format(Errors.ERROR_ON_FETCHING_TRACE, traceId), e);
			return Constants.DEFAULT_TRANSACTION_FEE;
		}
	}

	@Override
	public BigDecimal getSweepTransactionFee(String traceId) {
		try {
			TraceDto traceDto = waitForTraceReady(traceId);

			TraceDto.Trace trace = traceDto.getTraces().getFirst();
			List<String> txOrder = trace.getTransactionsOrder();
			Map<String, TraceDto.Trace.Transaction> txMap = trace.getTransactions();

			TraceDto.Trace.Transaction firstTx = txMap.get(txOrder.getFirst());
			String firstTxFee = firstTx.getTotalFees();
			TraceDto.Trace.Transaction.Message firstOutMsg = firstTx.getOutMsgs().getFirst();
			String firstTxFwdFee = firstOutMsg.getFwdFee();
			String forwardedTonAmount = firstOutMsg.getValue();

			TraceDto.Trace.Transaction finalTx = txMap.get(txOrder.getLast());
			if ("1".equalsIgnoreCase(finalTx.getInMsg().getValue()) && txOrder.size() > 4) {
				finalTx = txMap.get(txOrder.get(txOrder.size() - 2));
			}

			String finalTxFee = finalTx.getTotalFees();
			String excessTonAmount = finalTx.getInMsg().getValue();

			return new BigDecimal(firstTxFee).add(new BigDecimal(firstTxFwdFee))
					.add(new BigDecimal(finalTxFee))
					.add(new BigDecimal(forwardedTonAmount).subtract(new BigDecimal(excessTonAmount)));

		} catch (Exception e) {
			LOG.error(String.format(Errors.ERROR_ON_FETCHING_TRACE, traceId), e);
			return Constants.DEFAULT_SWEEP_TRANSACTION_FEE;
		}
	}

	private long getStorageFee(String address) {
		String emptyCell = CellBuilder.beginCell().endCell().toBase64();
		return tonCoreServiceHelper.getEstimateFees(address, emptyCell, "", "", true).getSourceFees().getStorageFee();
	}

	@Override
	public EstimateFeeResponse estimateManualTransactionFee(FeeEstimationRequest request) {
		try {
			long storageFeeHighLoadWallet;
			long storageFeeSenderJettonWallet;
			long storageFeeRecipientJettonWallet = 0;
			if (StringUtil.nullOrEmpty(request.getFromAddress())) {
				TonMainAccount mainAccounts = tonMainAccountService.getMainAccountDetail(request.getJettonMasterAddress()).getFirst();
				storageFeeHighLoadWallet = getStorageFee(mainAccounts.getAddress());
				storageFeeSenderJettonWallet = getStorageFee(mainAccounts.getJettonWalletAddress());
			} else {
				storageFeeHighLoadWallet = getStorageFee(request.getFromAddress());
				JettonWalletDto senderJettonWallet = tonCoreServiceHelper.getJettonWallet(request.getFromAddress(), request.getJettonMasterAddress());
				storageFeeSenderJettonWallet = getStorageFee(senderJettonWallet.getJettonWallets().getFirst().getAddress());
			}
			JettonWalletDto receiverJettonWallet = tonCoreServiceHelper.getJettonWallet(request.getToAddress(), request.getJettonMasterAddress());
			if (!CollectionUtil.nullOrEmpty(receiverJettonWallet.getJettonWallets())) {
				storageFeeRecipientJettonWallet = getStorageFee(receiverJettonWallet.getJettonWallets().getFirst().getAddress());
			} else {
				storageFeeRecipientJettonWallet = 10000057; // Default Fee For Future Storage Fee
			}
			int total = 0;
			ConfigParam configParam = TonConfigParam.getConfigByChainId(ExecutionContextUtil.getContext().getChainId());

			int step1Fwd = calculateFwdFee(5, 1488, configParam);
			int step1Gas = calculateGasFee(7600, configParam);
			total += (step1Fwd * 2 + step1Gas);
			int internalFwdFee = calculateFwdFee(7, 2149, configParam);
			total += (internalFwdFee - step1Fwd);

			int step2Fwd = calculateFwdFee(2, 1032, configParam);
			int step2Gas = calculateGasFee(1756, configParam);
			total += (step2Fwd + step2Gas);

			int step3Fwd = calculateFwdFee(21, 7543, configParam);
			int step3Gas = calculateGasFee(8706, configParam);
			total += (step3Fwd + step3Gas);

			int step4Gas = calculateGasFee(10024, configParam);
			total += step4Gas;

			int message1Fwd = calculateFwdFee(2, 752, configParam);
			int message2Fwd = calculateFwdFee(0, 0, configParam);
			int messageFwdTotal = (message1Fwd + message2Fwd);
			total += messageFwdTotal;

			int extraGas = calculateGasFee(577, configParam);
			total += extraGas;
			long storageTotal = storageFeeHighLoadWallet + storageFeeSenderJettonWallet + storageFeeRecipientJettonWallet;
			long reservedStorage = 9466772;
			BigDecimal finalFee = BigDecimal.valueOf(total + storageTotal + reservedStorage);
			return EstimateFeeResponse.builder().transactionFee(finalFee.multiply(BigDecimal.valueOf(1.1)).toBigInteger()).build();
		} catch (Exception e) {
			LOG.error("Error estimating manual transaction fee", e);
			return EstimateFeeResponse.builder().transactionFee(Constants.DEFAULT_TRANSACTION_FEE.toBigInteger()).build();
		}
	}

	private int calculateFwdFee(int cell, int bits, ConfigParam configParam) {
		return (int) (configParam.getLumpPrice() + Math.ceil((double) (configParam.getBitPrice() * bits + configParam.getCellPrice() * cell) / BIT16));
	}

	private int calculateGasFee(int gasUsed, ConfigParam configParam) {
		return (int) Math.ceil((double) gasUsed * configParam.getGasPrice() / BIT16);
	}
}

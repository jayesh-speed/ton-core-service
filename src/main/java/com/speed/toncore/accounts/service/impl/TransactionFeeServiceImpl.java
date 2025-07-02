package com.speed.toncore.accounts.service.impl;

import com.speed.toncore.accounts.service.TransactionFeeService;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.pojo.TraceDto;
import com.speed.toncore.ton.TonCoreServiceHelper;
import com.speed.toncore.util.LogMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionFeeServiceImpl implements TransactionFeeService {

	private final TonCoreServiceHelper tonCoreServiceHelper;

	@Override
	public BigDecimal getJettonTransactionFee(String traceId) {
		try {
			LOG.info(String.format(LogMessages.Info.WAITING_FOR_TRACE_UPDATE, traceId));
			Thread.sleep(10000);
			TraceDto traceDto = tonCoreServiceHelper.getTraceByTraceId(traceId);
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
			LOG.info(String.format(LogMessages.Info.WAITING_FOR_TRACE_UPDATE, traceId));
			Thread.sleep(10000);
			TraceDto traceDto = tonCoreServiceHelper.getTraceByTraceId(traceId);
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
}

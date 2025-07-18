package com.speed.toncore.accounts.service.impl;

import com.speed.javacommon.exceptions.InternalServerErrorException;
import com.speed.javacommon.util.StringUtil;
import com.speed.toncore.accounts.request.FeeEstimationRequest;
import com.speed.toncore.accounts.response.EstimateFeeResponse;
import com.speed.toncore.accounts.service.TonMainAccountService;
import com.speed.toncore.accounts.service.TransactionFeeService;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.domain.model.TonMainAccount;
import com.speed.toncore.enums.BalancePreference;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import com.speed.toncore.pojo.TraceDto;
import com.speed.toncore.schedular.ConfigParam;
import com.speed.toncore.schedular.TonConfigParam;
import com.speed.toncore.tokens.response.TonTokenResponse;
import com.speed.toncore.tokens.service.TonTokenService;
import com.speed.toncore.ton.TonCoreService;
import com.speed.toncore.ton.TonCoreServiceHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ton.ton4j.cell.CellBuilder;
import org.ton.ton4j.utils.Utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionFeeServiceImpl implements TransactionFeeService {

	private static final int BIT16 = 65536;   // 2^16, used for fee calculations
	private final TonCoreServiceHelper tonCoreServiceHelper;
	private final TonMainAccountService tonMainAccountService;
	private final TonTokenService tonTokenService;
	private final TonCoreService tonCoreService;

	@Override
	public BigDecimal getTokenTransferFee(String traceId) {
		try {
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

			return Utils.fromNano(new BigDecimal(firstTxFee).add(new BigDecimal(firstTxFwdFee))
					.add(new BigDecimal(secondTxFee))
					.add(new BigDecimal(secondTxFwdFee))
					.add(new BigDecimal(finalTxFee))
					.add(new BigDecimal(forwardedTonAmount).subtract(new BigDecimal(excessTonAmount))), 9);

		} catch (Exception e) {
			LOG.error(String.format(Errors.ERROR_ON_FETCHING_TRACE, traceId), e);
			return Constants.DEFAULT_TRANSACTION_FEE;
		}
	}

	@Override
	public BigDecimal getSweepFee(String traceId) {
		try {
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

			return Utils.fromNano(new BigDecimal(firstTxFee).add(new BigDecimal(firstTxFwdFee))
					.add(new BigDecimal(finalTxFee))
					.add(new BigDecimal(forwardedTonAmount).subtract(new BigDecimal(excessTonAmount))), 9);

		} catch (Exception e) {
			LOG.error(String.format(Errors.ERROR_ON_FETCHING_TRACE, traceId), e);
			return Constants.DEFAULT_SWEEP_TRANSACTION_FEE;
		}
	}

	@Override
	public EstimateFeeResponse estimateTransactionFee(FeeEstimationRequest request) {
		TonTokenResponse tonToken = tonTokenService.getTonTokenByAddress(request.getTokenAddress());
		if (Objects.isNull(tonToken)) {
			throw new InternalServerErrorException(Errors.TOKEN_ADDRESS_NOT_SUPPORTED);
		}
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
		long storageFeeMainAccount;
		long storageFeeSenderTokenContract;
		long storageFeeRecipientTokenContract = 0;
		long reservedStorage = 0;
		String fromAddress = request.getFromAddress();
		String toAddress = request.getToAddress();
		String tokenAddress = request.getTokenAddress();
		try {
			if (StringUtil.nullOrEmpty(fromAddress)) {
				TonMainAccount mainAccount = tonMainAccountService.getMainAccountInternal(tokenAddress, BalancePreference.MAX_BALANCE);
				storageFeeMainAccount = getStorageFee(mainAccount.getAddress());
				storageFeeSenderTokenContract = getStorageFee(mainAccount.getTokenContractAddress());
			} else {
				storageFeeMainAccount = getStorageFee(fromAddress);
				String senderTokenContractAddress = tonCoreService.getTokenContractAddress(fromAddress, tokenAddress);
				storageFeeSenderTokenContract = getStorageFee(senderTokenContractAddress);
			}

			String recipientTokenContractAddress = tonCoreService.getTokenContractAddress(toAddress, tokenAddress);
			if (recipientTokenContractAddress != null) {
				storageFeeRecipientTokenContract = getStorageFee(recipientTokenContractAddress);
			} else {
				reservedStorage = tonToken.getReserveStorageFee();
			}
			ConfigParam configParam = TonConfigParam.getConfigByChainId(chainId);
			int fwdFee = calculateFwdFee(tonToken.getNoOfCell(), tonToken.getNoOfBits(), configParam);
			int gasFee = calculateGasFee(tonToken.getGasUnit(), configParam);
			long storageTotal = storageFeeMainAccount + storageFeeSenderTokenContract + storageFeeRecipientTokenContract;
			long totalFee = fwdFee + gasFee + storageTotal + reservedStorage;

			return EstimateFeeResponse.builder()
					.chainId(chainId)
					.mainNet(chainId.equals(Constants.MAIN_NET_CHAIN_ID))
					.estimateFee(Utils.fromNano(totalFee))
					.build();

		} catch (Exception e) {
			LOG.error(String.format(Errors.ERROR_WHILE_ESTIMATION_FEE, chainId), e);
			return EstimateFeeResponse.builder().estimateFee(Constants.DEFAULT_TRANSACTION_FEE).build();
		}
	}

	@Override
	public BigInteger estimateSweepFee(String feeAccountAddress, String spenderAccountAddress, String mainAccountTokenContractAddress,
			String tokenAddress) {
		TonTokenResponse token = tonTokenService.getTonTokenByAddress(tokenAddress);
		if (Objects.isNull(token)) {
			throw new InternalServerErrorException(Errors.TOKEN_ADDRESS_NOT_SUPPORTED);
		}
		long feeAccountStorageFee = getStorageFee(feeAccountAddress);
		long mainAccountTokenContractStorageFee = getStorageFee(mainAccountTokenContractAddress);
		long spenderTokenContractStorageFee = getStorageFee(tonCoreService.getTokenContractAddress(spenderAccountAddress, tokenAddress));
		long spenderAccountStorageFee = getStorageFee(spenderAccountAddress);
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
		ConfigParam configParam = TonConfigParam.getConfigByChainId(chainId);
		int fwdFee = calculateFwdFee(token.getNoOfCellV3(), token.getNoOfBitsV3(), configParam);
		int gasFee = calculateGasFee(token.getGasUnitV3(), configParam);
		long reservedStorage = token.getReserveStorageFee();
		long totalFee =
				fwdFee + gasFee + feeAccountStorageFee + mainAccountTokenContractStorageFee + spenderTokenContractStorageFee + spenderAccountStorageFee +
						reservedStorage;
		return new BigInteger(String.valueOf((long) (totalFee * 1.20))); // Adding 20% buffer to the fee
	}

	private long getStorageFee(String address) {
		if (StringUtil.nonNullNonEmpty(address)) {
			String emptyCell = CellBuilder.beginCell().endCell().toBase64();
			return tonCoreServiceHelper.getEstimateFees(address, emptyCell, "", "", true).getSourceFees().getStorageFee();
		}
		return 0;
	}

	private int calculateFwdFee(int cellCount, int bitCount, ConfigParam config) {
		double dataCost = (double) (config.getBitPrice() * bitCount + config.getCellPrice() * cellCount) / BIT16;
		return (int) (5 * config.getLumpPrice() + Math.ceil(dataCost));
	}

	private int calculateGasFee(int gasUsed, ConfigParam config) {
		return (int) Math.ceil((double) gasUsed * config.getGasPrice() / BIT16);
	}
}

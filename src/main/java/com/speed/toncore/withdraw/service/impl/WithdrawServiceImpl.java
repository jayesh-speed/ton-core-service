package com.speed.toncore.withdraw.service.impl;

import com.speed.javacommon.exceptions.BadRequestException;
import com.speed.javacommon.exceptions.InternalServerErrorException;
import com.speed.javacommon.util.CollectionUtil;
import com.speed.javacommon.util.StringUtil;
import com.speed.toncore.accounts.request.FeeEstimationRequest;
import com.speed.toncore.accounts.service.TonMainAccountService;
import com.speed.toncore.accounts.service.TransactionFeeService;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.domain.model.TonMainAccount;
import com.speed.toncore.jettons.response.TonJettonResponse;
import com.speed.toncore.jettons.service.TonJettonService;
import com.speed.toncore.service.OnChainTxService;
import com.speed.toncore.ton.TonCoreService;
import com.speed.toncore.util.TonUtil;
import com.speed.toncore.withdraw.request.WithdrawRequest;
import com.speed.toncore.withdraw.response.WithdrawResponse;
import com.speed.toncore.withdraw.service.WithdrawService;
import org.springframework.stereotype.Service;
import org.ton.ton4j.utils.Utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class WithdrawServiceImpl implements WithdrawService {

	private final TonJettonService tonJettonService;
	private final TonMainAccountService tonMainAccountService;
	private final TonCoreService tonCoreService;
	private final OnChainTxService onChainTxService;
	private final TransactionFeeService transactionFeeService;

	public WithdrawServiceImpl(TonJettonService tonJettonService, TonMainAccountService tonMainAccountService, TonCoreService tonCoreService,
			OnChainTxService onChainTxService, TransactionFeeService transactionFeeService) {
		this.tonJettonService = tonJettonService;
		this.tonMainAccountService = tonMainAccountService;
		this.tonCoreService = tonCoreService;
		this.onChainTxService = onChainTxService;
		this.transactionFeeService = transactionFeeService;
	}

	@Override
	public WithdrawResponse transferJetton(WithdrawRequest withdrawRequest) {
		if (StringUtil.nullOrEmpty(withdrawRequest.getJettonMasterAddress()) && StringUtil.nullOrEmpty(withdrawRequest.getJettonSymbol())) {
			throw new BadRequestException(Errors.JETTON_INFO_MISSING, null, null);
		}
		TonJettonResponse jetton;
		if (StringUtil.nonNullNonEmpty(withdrawRequest.getJettonMasterAddress())) {
			jetton = tonJettonService.getTonJettonByAddress(withdrawRequest.getJettonMasterAddress());
			if (Objects.isNull(jetton)) {
				throw new BadRequestException(Errors.JETTON_ADDRESS_NOT_SUPPORTED, null, null);
			}
		} else {
			jetton = tonJettonService.getTonJettonBySymbol(withdrawRequest.getJettonSymbol());
			if (Objects.isNull(jetton)) {
				throw new BadRequestException(Errors.JETTON_SYMBOL_NOT_SUPPORTED, null, null);
			}
		}
		withdrawRequest.setJettonMasterAddress(jetton.getJettonMasterAddress());
		BigDecimal value = new BigDecimal(withdrawRequest.getValue());
		List<TonMainAccount> mainAccountList = tonMainAccountService.getMainAccountDetail(jetton.getJettonMasterAddress());
		if (CollectionUtil.nullOrEmpty(mainAccountList)) {
			throw new InternalServerErrorException(Errors.MAIN_ACCOUNT_NOT_FOUND);
		}
		Map<TonMainAccount, BigDecimal> accountBalanceMap = mainAccountList.stream()
				.collect(Collectors.toMap(acc -> acc,
						acc -> tonCoreService.fetchJettonBalance(jetton.getJettonMasterAddress(), acc.getAddress(), jetton.getDecimals())));
		Map.Entry<TonMainAccount, BigDecimal> maxBalanceAccMap = accountBalanceMap.entrySet()
				.stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.toList()
				.getFirst();
		BigDecimal tokenBalanceOnAccount = maxBalanceAccMap.getValue();
		if (tokenBalanceOnAccount.compareTo(value) < 0) {
			throw new InternalServerErrorException(String.format(Errors.INSUFFICIENT_MAIN_ACC_BALANCE, jetton.getJettonSymbol()));
		}
		TonMainAccount maxBalanceAcc = maxBalanceAccMap.getKey();
		BigDecimal tonBalanceOnAccount = tonCoreService.fetchTonBalance(maxBalanceAcc.getAddress());
		if (tonBalanceOnAccount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new InternalServerErrorException(String.format(Errors.INSUFFICIENT_FEE_BALANCE, jetton.getJettonSymbol()));
		}
		withdrawRequest.setFromAddress(maxBalanceAcc.getAddress());
		String txReference = TonUtil.generateTransferTransactionReference();
		BigInteger estimatedFee = estimateFee(withdrawRequest.getFromAddress(), withdrawRequest.getToAddress(), jetton.getJettonMasterAddress());
		String transactionHash = tonCoreService.transferJettons(withdrawRequest.getFromAddress(), withdrawRequest.getToAddress(), jetton, value,
				maxBalanceAcc.getSecretKey(), maxBalanceAcc.getJettonWalletAddress(), txReference, estimatedFee);
		onChainTxService.createOnChainDebitTx(transactionHash, withdrawRequest, txReference);
		return WithdrawResponse.builder()
				.transactionHash(transactionHash)
				.txReference(txReference)
				.fromAddress(withdrawRequest.getFromAddress())
				.toAddress(withdrawRequest.getToAddress())
				.value(withdrawRequest.getValue())
				.build();
	}

	@Override
	public void updateLatestLogicalTime(String id, Long logicalTime) {
		onChainTxService.updateLatestLogicalTime(id, logicalTime);
	}

	private BigInteger estimateFee(String fromAddress, String toAddress, String jettonMasterAddress) {
		FeeEstimationRequest feeEstimationRequest = new FeeEstimationRequest();
		feeEstimationRequest.setFromAddress(fromAddress);
		feeEstimationRequest.setToAddress(toAddress);
		feeEstimationRequest.setJettonMasterAddress(jettonMasterAddress);
		BigDecimal scaledFee = transactionFeeService.estimateTransactionFee(feeEstimationRequest)
				.getTransactionFee()
				.multiply(BigDecimal.valueOf(1.1))
				.setScale(9, RoundingMode.HALF_UP);

		return Utils.toNano(scaledFee);
	}
}

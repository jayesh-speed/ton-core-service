package com.speed.toncore.withdraw.service.impl;

import com.speed.javacommon.exceptions.BadRequestException;
import com.speed.javacommon.exceptions.InternalServerErrorException;
import com.speed.javacommon.util.StringUtil;
import com.speed.toncore.accounts.request.FeeEstimationRequest;
import com.speed.toncore.accounts.service.TonMainAccountService;
import com.speed.toncore.accounts.service.TransactionFeeService;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.domain.model.TonMainAccount;
import com.speed.toncore.enums.BalancePreference;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import com.speed.toncore.service.OnChainTxService;
import com.speed.toncore.tokens.response.TonTokenResponse;
import com.speed.toncore.tokens.service.TonTokenService;
import com.speed.toncore.ton.TonCoreService;
import com.speed.toncore.util.TonUtil;
import com.speed.toncore.withdraw.request.WithdrawRequest;
import com.speed.toncore.withdraw.response.WithdrawResponse;
import com.speed.toncore.withdraw.service.WithdrawService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ton.ton4j.utils.Utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WithdrawServiceImpl implements WithdrawService {

	private final TonTokenService tonTokenService;
	private final TonMainAccountService tonMainAccountService;
	private final TonCoreService tonCoreService;
	private final OnChainTxService onChainTxService;
	private final TransactionFeeService transactionFeeService;

	@Override
	public WithdrawResponse transferToken(WithdrawRequest withdrawRequest) {
		if (StringUtil.nullOrEmpty(withdrawRequest.getTokenAddress()) && StringUtil.nullOrEmpty(withdrawRequest.getTokenSymbol())) {
			throw new BadRequestException(Errors.TOKEN_INFO_MISSING, null, null);
		}
		TonTokenResponse token = Optional.ofNullable(withdrawRequest.getTokenAddress())
				.filter(StringUtil::nonNullNonEmpty)
				.map(tonTokenService::getTonTokenByAddress)
				.orElseGet(() -> tonTokenService.getTonTokenBySymbol(withdrawRequest.getTokenSymbol()));

		if (Objects.isNull(token)) {
			throw new BadRequestException(StringUtil.nonNullNonEmpty(withdrawRequest.getTokenAddress())
					? Errors.TOKEN_ADDRESS_NOT_SUPPORTED
					: Errors.TOKEN_SYMBOL_NOT_SUPPORTED, null, null);
		}

		withdrawRequest.setTokenAddress(token.getTokenAddress());
		BigDecimal value = new BigDecimal(withdrawRequest.getValue());
		TonMainAccount mainAccount = selectTonMainAccount(token.getTokenAddress());
		BigDecimal tokenBalanceOnAccount = tonCoreService.fetchTokenBalance(token.getTokenAddress(), mainAccount.getAddress(), token.getDecimals());
		if (tokenBalanceOnAccount.compareTo(value) < 0) {
			throw new InternalServerErrorException(String.format(Errors.INSUFFICIENT_MAIN_ACC_BALANCE, token.getTokenSymbol()));
		}
		BigDecimal tonBalanceOnAccount = tonCoreService.fetchTonBalance(mainAccount.getAddress());
		if (tonBalanceOnAccount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new InternalServerErrorException(String.format(Errors.INSUFFICIENT_FEE_BALANCE, token.getTokenSymbol()));
		}
		withdrawRequest.setFromAddress(mainAccount.getAddress());
		String txReference = TonUtil.generateTransferTransactionReference();
		BigInteger estimatedFee = estimateFee(withdrawRequest.getFromAddress(), withdrawRequest.getToAddress(), token.getTokenAddress());
		String transactionHash = tonCoreService.transferTokens(withdrawRequest.getFromAddress(), withdrawRequest.getToAddress(), token, value,
				mainAccount.getPrivateKey(), mainAccount.getTokenContractAddress(), txReference, estimatedFee);
		onChainTxService.createOnChainDebitTx(transactionHash, withdrawRequest, txReference);
		return WithdrawResponse.builder()
				.transactionHash(transactionHash)
				.txReference(txReference)
				.fromAddress(withdrawRequest.getFromAddress())
				.toAddress(withdrawRequest.getToAddress())
				.value(withdrawRequest.getValue())
				.build();
	}

	private TonMainAccount selectTonMainAccount(String tokenAddress) {
		TonMainAccount mainAccount;
		do {
			mainAccount = tonMainAccountService.getMainAccountInternal(tokenAddress, BalancePreference.MAX_BALANCE);
			if (Objects.isNull(mainAccount)) {
				throw new InternalServerErrorException(
						String.format(Errors.VALID_ACCOUNT_NOT_FOUND, tokenAddress, ExecutionContextUtil.getContext().getChainId()));
			}
			if (tonCoreService.isActive(mainAccount.getTokenContractAddress())) {
				return mainAccount;
			} else {
				tonMainAccountService.updateAccountStatusInActive(mainAccount.getAddress());
			}
		} while (true);
	}

	@Override
	public void updateLatestLogicalTime(String id, Long logicalTime) {
		onChainTxService.updateLatestLogicalTime(id, logicalTime);
	}

	private BigInteger estimateFee(String fromAddress, String toAddress, String tokenAddress) {
		FeeEstimationRequest feeEstimationRequest = new FeeEstimationRequest();
		feeEstimationRequest.setFromAddress(fromAddress);
		feeEstimationRequest.setToAddress(toAddress);
		feeEstimationRequest.setTokenAddress(tokenAddress);
		BigDecimal scaledFee = transactionFeeService.estimateTransactionFee(feeEstimationRequest)
				.getEstimateFee()
				.multiply(BigDecimal.valueOf(1.2)) // Buffer 20% for fee estimation
				.setScale(9, RoundingMode.HALF_UP);

		return Utils.toNano(scaledFee);
	}
}

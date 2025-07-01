package com.speed.toncore.withdraw.service.impl;

import com.speed.javacommon.exceptions.BadRequestException;
import com.speed.javacommon.exceptions.InternalServerErrorException;
import com.speed.javacommon.util.CollectionUtil;
import com.speed.javacommon.util.StringUtil;
import com.speed.toncore.accounts.service.TonMainAccountService;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.domain.model.TonMainAccount;
import com.speed.toncore.jettons.response.TonJettonResponse;
import com.speed.toncore.jettons.service.TonJettonService;
import com.speed.toncore.withdraw.request.WithdrawRequest;
import com.speed.toncore.withdraw.response.WithdrawResponse;
import com.speed.toncore.withdraw.service.WithdrawService;
import com.speed.toncore.service.OnChainTxService;
import com.speed.toncore.ton.TonCoreService;
import com.speed.toncore.util.TonUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

	public WithdrawServiceImpl(TonJettonService tonJettonService, TonMainAccountService tonMainAccountService, TonCoreService tonCoreService,
			OnChainTxService onChainTxService) {
		this.tonJettonService = tonJettonService;
		this.tonMainAccountService = tonMainAccountService;
		this.tonCoreService = tonCoreService;
		this.onChainTxService = onChainTxService;
	}

	@Override
	public WithdrawResponse transferJetton(WithdrawRequest withdrawRequest) {
		if (StringUtil.nullOrEmpty(withdrawRequest.getJettonAddress()) && StringUtil.nullOrEmpty(withdrawRequest.getJettonSymbol())) {
			throw new BadRequestException(Errors.JETTON_INFO_MISSING, null, null);
		}
		TonJettonResponse jetton;
		if (StringUtil.nonNullNonEmpty(withdrawRequest.getJettonAddress())) {
			jetton = tonJettonService.getTonJettonByAddress(withdrawRequest.getJettonAddress());
			if (Objects.isNull(jetton)) {
				throw new BadRequestException(Errors.JETTON_ADDRESS_NOT_SUPPORTED, null, null);
			}
		} else {
			jetton = tonJettonService.getTonJettonBySymbol(withdrawRequest.getJettonSymbol());
			if (Objects.isNull(jetton)) {
				throw new BadRequestException(Errors.JETTON_SYMBOL_NOT_SUPPORTED, null, null);
			}
		}
		withdrawRequest.setJettonAddress(jetton.getJettonMasterAddress());
		BigDecimal value = new BigDecimal(withdrawRequest.getValue());
		List<TonMainAccount> mainAccountList = List.of(tonMainAccountService.getMainAccountDetail(jetton.getJettonMasterAddress()));
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
		withdrawRequest.setFromAddress(maxBalanceAcc.getAddress());
		String txReference = TonUtils.generateTransferTransactionReference();
		String transactionHash = tonCoreService.transferJettons(withdrawRequest.getFromAddress(), withdrawRequest.getToAddress(),
				withdrawRequest.getJettonAddress(), value, maxBalanceAcc.getSecretKey(), txReference, maxBalanceAcc.getJettonWalletAddress(),
				jetton.getDecimals());
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
}

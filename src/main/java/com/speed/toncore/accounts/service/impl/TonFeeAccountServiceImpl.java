package com.speed.toncore.accounts.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.speed.javacommon.exceptions.BadRequestException;
import com.speed.javacommon.exceptions.InternalServerErrorException;
import com.speed.javacommon.util.DateTimeUtil;
import com.speed.javacommon.util.StringUtil;
import com.speed.toncore.accounts.response.DeployedAccountResponse;
import com.speed.toncore.accounts.response.TonAccountResponse;
import com.speed.toncore.accounts.service.TonFeeAccountService;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.domain.model.QTonFeeAccount;
import com.speed.toncore.domain.model.TonFeeAccount;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import com.speed.toncore.repository.TonFeeAccountRepository;
import com.speed.toncore.ton.TonCoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TonFeeAccountServiceImpl implements TonFeeAccountService {

	private final TonFeeAccountRepository tonFeeAccountRepository;
	private final TonWalletServiceHelper tonWalletServiceHelper;
	private final QTonFeeAccount qTonFeeAccount = QTonFeeAccount.tonFeeAccount;
	private final TonCoreService tonCoreService;

	@Override
	public TonAccountResponse createFeeAccount() {
		TonFeeAccount feeAccount = tonWalletServiceHelper.createFeeAccount();
		return TonAccountResponse.builder().address(feeAccount.getAddress()).publicKey(feeAccount.getPublicKey()).build();
	}

	@Override
	public void deleteFeeAccount(String address) {
		Predicate queryPredicate = new BooleanBuilder(qTonFeeAccount.address.eq(address)).and(
				qTonFeeAccount.chainId.eq(ExecutionContextUtil.getContext().getChainId()));
		tonFeeAccountRepository.deleteByPredicate(queryPredicate, qTonFeeAccount);
	}

	@Override
	public DeployedAccountResponse deployFeeAccount(String address) {
		String hash = tonWalletServiceHelper.deployFeeAccount(address);
		if (StringUtil.nullOrEmpty(hash)) {
			throw new InternalServerErrorException(String.format(Errors.ERROR_DEPLOY_FEE_ACCOUNT, address));
		}
		Predicate queryPredicate = qTonFeeAccount.address.eq(address).and(qTonFeeAccount.chainId.eq(ExecutionContextUtil.getContext().getChainId()));
		Long currentTime = DateTimeUtil.currentEpochMilliSecondsUTC();
		Map<Path<?>, Object> fieldWithValue = HashMap.newHashMap(2);
		fieldWithValue.put(qTonFeeAccount.deploymentTxHash, hash);
		fieldWithValue.put(qTonFeeAccount.modified, currentTime);
		long count = tonFeeAccountRepository.updateFields(queryPredicate, qTonFeeAccount, fieldWithValue);
		if (!(count > 0)) {
			throw new BadRequestException(String.format(Errors.NO_FEE_ACCOUNT, address), null, null);
		}
		return DeployedAccountResponse.builder().transactionHash(hash).build();
	}

	@Override
	public TonAccountResponse getTonBalance(String address) {
		BooleanBuilder queryPredicate = new BooleanBuilder(qTonFeeAccount.address.eq(address)).and(
				qTonFeeAccount.chainId.eq(ExecutionContextUtil.getContext().getChainId()));
		TonFeeAccount feeAccount = tonFeeAccountRepository.findAndProjectUnique(queryPredicate, qTonFeeAccount, qTonFeeAccount.id,
				qTonFeeAccount.address, qTonFeeAccount.publicKey, qTonFeeAccount.tonBalance);
		if (Objects.isNull(feeAccount)) {
			throw new BadRequestException(String.format(Errors.NO_FEE_ACCOUNT, address), null, null);
		}
		return TonAccountResponse.builder()
				.address(feeAccount.getAddress())
				.publicKey(feeAccount.getPublicKey())
				.localBalance(feeAccount.getTonBalance().compareTo(BigDecimal.ZERO) == 0
						? String.valueOf(BigDecimal.ZERO)
						: String.valueOf(feeAccount.getTonBalance()))
				.build();
	}

	@Override
	public TonAccountResponse updateFeeAccountLocalBalance(String address) {
		Predicate queryPredicate = qTonFeeAccount.address.eq(address).and(qTonFeeAccount.chainId.eq(ExecutionContextUtil.getContext().getChainId()));
		BigDecimal tonBalance = tonCoreService.fetchTonBalance(address);
		Long currentTime = DateTimeUtil.currentEpochMilliSecondsUTC();
		Map<Path<?>, Object> fieldWithValue = HashMap.newHashMap(2);
		fieldWithValue.put(qTonFeeAccount.tonBalance, tonBalance);
		fieldWithValue.put(qTonFeeAccount.modified, currentTime);
		long count = tonFeeAccountRepository.updateFields(queryPredicate, qTonFeeAccount, fieldWithValue);
		if (!(count > 0)) {
			throw new BadRequestException(String.format(Errors.NO_FEE_ACCOUNT, address), null, null);
		}
		TonFeeAccount feeAccount = tonFeeAccountRepository.findAndProjectUnique(queryPredicate, qTonFeeAccount, qTonFeeAccount.id,
				qTonFeeAccount.address, qTonFeeAccount.publicKey, qTonFeeAccount.tonBalance);
		return TonAccountResponse.builder()
				.address(feeAccount.getAddress())
				.publicKey(feeAccount.getPublicKey())
				.localBalance(feeAccount.getTonBalance().compareTo(BigDecimal.ZERO) == 0
						? String.valueOf(BigDecimal.ZERO)
						: String.valueOf(feeAccount.getTonBalance()))
				.build();
	}

	@Override
	public TonAccountResponse getFeeAccount() {
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
		Predicate queryPredicate = qTonFeeAccount.chainId.eq(chainId);
		TonFeeAccount feeAccount = tonFeeAccountRepository.findAndProjectUnique(queryPredicate, qTonFeeAccount, qTonFeeAccount.address,
				qTonFeeAccount.publicKey, qTonFeeAccount.id, qTonFeeAccount.tonBalance);
		return TonAccountResponse.builder()
				.address(feeAccount.getAddress())
				.publicKey(feeAccount.getPublicKey())
				.localBalance(feeAccount.getTonBalance().compareTo(BigDecimal.ZERO) == 0
						? String.valueOf(BigDecimal.ZERO)
						: String.valueOf(feeAccount.getTonBalance()))
				.build();
	}

	@Override
	public TonFeeAccount getTonFeeAccount() {
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
		Predicate queryPredicate = qTonFeeAccount.chainId.eq(chainId);
		return tonFeeAccountRepository.findAndProjectUnique(queryPredicate, qTonFeeAccount, qTonFeeAccount.address, qTonFeeAccount.publicKey,
				qTonFeeAccount.id, qTonFeeAccount.tonBalance, qTonFeeAccount.secretKey, qTonFeeAccount.address);
	}
}

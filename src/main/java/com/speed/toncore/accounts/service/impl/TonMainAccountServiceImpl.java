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
import com.speed.toncore.accounts.service.TonMainAccountService;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.domain.model.QTonMainAccount;
import com.speed.toncore.domain.model.TonMainAccount;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import com.speed.toncore.repository.TonMainAccountRepository;
import com.speed.toncore.ton.TonCoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TonMainAccountServiceImpl implements TonMainAccountService {

	private final TonMainAccountRepository tonMainAccountRepository;
	private final TonWalletServiceHelper tonWalletServiceHelper;
	private final QTonMainAccount qTonMainAccount = QTonMainAccount.tonMainAccount;
	private final TonCoreService tonCoreService;

	@Override
	public TonAccountResponse createMainAccount(String jettonMasterAddress) {
		TonMainAccount mainAccount = tonWalletServiceHelper.createMainAccount(jettonMasterAddress);
		return TonAccountResponse.builder().address(mainAccount.getAddress()).build();
	}

	@Override
	public void deleteMainAccount(String address) {
		Predicate queryPredicate = new BooleanBuilder(qTonMainAccount.address.eq(address)).and(
				qTonMainAccount.chainId.eq(ExecutionContextUtil.getContext().getChainId()));
		tonMainAccountRepository.deleteByPredicate(queryPredicate, qTonMainAccount);
	}

	@Override
	public DeployedAccountResponse deployMainAccount(String address) {
		String hash = tonWalletServiceHelper.createDeployMainAccountMessage(address);
		if (StringUtil.nullOrEmpty(hash)) {
			throw new InternalServerErrorException(String.format(Errors.ERROR_DEPLOY_MAIN_ACCOUNT, address));
		}
		Predicate queryPredicate = qTonMainAccount.address.eq(address).and(qTonMainAccount.chainId.eq(ExecutionContextUtil.getContext().getChainId()));
		Long currentTime = DateTimeUtil.currentEpochMilliSecondsUTC();
		Map<Path<?>, Object> fieldWithValue = HashMap.newHashMap(2);
		fieldWithValue.put(qTonMainAccount.deploymentTxHash, hash);
		fieldWithValue.put(qTonMainAccount.modified, currentTime);
		long count = tonMainAccountRepository.updateFields(queryPredicate, qTonMainAccount, fieldWithValue);
		if (!(count > 0)) {
			throw new BadRequestException(String.format(Errors.NO_MAIN_ACCOUNT, address), null, null);
		}
		return DeployedAccountResponse.builder().transactionHash(hash).build();
	}

	@Override
	public TonAccountResponse getMainAccount(String jettonAddress) {
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
		Predicate queryPredicate = qTonMainAccount.chainId.eq(chainId).and(qTonMainAccount.jettonMasterAddress.eq(jettonAddress));
		TonMainAccount mainAccount = tonMainAccountRepository.findAndProjectUnique(queryPredicate, qTonMainAccount, qTonMainAccount.address,
				qTonMainAccount.publicKey, qTonMainAccount.id, qTonMainAccount.tonBalance);
		return TonAccountResponse.builder()
				.address(mainAccount.getAddress())
				.localBalance(mainAccount.getTonBalance())
				.build();
	}

	@Override
	public TonMainAccount getMainAccountDetail(String jettonAddress) {
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
		Predicate queryPredicate = qTonMainAccount.chainId.eq(chainId).and(qTonMainAccount.jettonMasterAddress.eq(jettonAddress));
		return tonMainAccountRepository.findAndProjectUnique(queryPredicate, qTonMainAccount, qTonMainAccount.address, qTonMainAccount.publicKey,
				qTonMainAccount.id, qTonMainAccount.tonBalance, qTonMainAccount.secretKey, qTonMainAccount.jettonWalletAddress);
	}
}

package com.speed.toncore.accounts.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.speed.javacommon.exceptions.BadRequestException;
import com.speed.javacommon.util.DateTimeUtil;
import com.speed.toncore.accounts.response.DeployedAccountResponse;
import com.speed.toncore.accounts.response.TonAccountResponse;
import com.speed.toncore.accounts.service.TonFeeAccountService;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.domain.model.QTonFeeAccount;
import com.speed.toncore.domain.model.TonFeeAccount;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import com.speed.toncore.repository.TonFeeAccountRepository;
import com.speed.toncore.ton.TonCoreService;
import com.speed.toncore.ton.TonNode;
import com.speed.toncore.ton.TonNodePool;
import com.speed.toncore.util.SecurityManagerUtil;
import com.speed.toncore.util.TonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ton.ton4j.smartcontract.wallet.v3.WalletV3R2;
import org.ton.ton4j.utils.Utils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TonFeeAccountServiceImpl implements TonFeeAccountService {

	private static final QTonFeeAccount qTonFeeAccount = QTonFeeAccount.tonFeeAccount;
	private final TonFeeAccountRepository tonFeeAccountRepository;
	private final TonNodePool tonNodePool;
	private final TonCoreService tonCoreService;

	private Predicate getFeeAccountQueryPredicate(String address) {
		return new BooleanBuilder(qTonFeeAccount.address.eq(address)).and(qTonFeeAccount.chainId.eq(ExecutionContextUtil.getContext().getChainId()));
	}

	@Override
	public TonAccountResponse createFeeAccount() {
		TonNode tonNode = tonNodePool.getTonNodeByChainId();
		String encryptionAlgo = tonNode.getEncryptionAlgo();
		byte[] encryptionKey = tonNode.getEncryptionKey();
		WalletV3R2 feeAccount = WalletV3R2.builder().walletId(tonNode.getWalletId()).keyPair(Utils.generateSignatureKeyPair()).build();
		String encryptedPriKey = SecurityManagerUtil.encrypt(encryptionAlgo, Utils.bytesToHex(feeAccount.getKeyPair().getSecretKey()), encryptionKey);
		TonFeeAccount tonFeeAccount = TonFeeAccount.builder()
				.address(feeAccount.getAddress().toRaw())
				.publicKey(Utils.bytesToHex(feeAccount.getKeyPair().getPublicKey()))
				.privateKey(encryptedPriKey)
				.addressType(feeAccount.getName())
				.chainId(tonNode.getChainId())
				.tonBalance(BigDecimal.ZERO)
				.mainNet(tonNode.isMainNet())
				.build();
		tonFeeAccountRepository.save(tonFeeAccount);
		return TonAccountResponse.builder().address(tonFeeAccount.getAddress()).build();
	}

	@Override
	public void deleteFeeAccount(String address) {
		address = TonUtil.toRawAddress(address);
		Predicate queryPredicate = getFeeAccountQueryPredicate(address);
		tonFeeAccountRepository.deleteByPredicate(queryPredicate, qTonFeeAccount);
	}

	@Override
	public DeployedAccountResponse deployFeeAccount(String address) {
		String rawAddress = TonUtil.toRawAddress(address);
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
		Predicate queryPredicate = getFeeAccountQueryPredicate(rawAddress);
		TonFeeAccount tonFeeAccount = tonFeeAccountRepository.findOne(queryPredicate)
				.orElseThrow(() -> new BadRequestException(String.format(Errors.FEE_ACCOUNT_NOT_FOUND, rawAddress, chainId), null, null));
		if (tonCoreService.isDeployed(rawAddress)) {
			throw new BadRequestException(String.format(Errors.ACCOUNT_ALREADY_DEPLOYED, rawAddress), null, null);
		}
		BigDecimal accountBalance = tonCoreService.fetchTonBalance(rawAddress);
		if (accountBalance.compareTo(BigDecimal.ZERO) <= 0) {
			throw new BadRequestException(String.format(Errors.NOT_ENOUGH_FUNDS_TO_DEPLOY, rawAddress), null, null);
		}
		String hash = tonCoreService.deployFeeAccount(tonFeeAccount.getPrivateKey());
		Map<Path<?>, Object> fieldWithValue = HashMap.newHashMap(2);
		fieldWithValue.put(qTonFeeAccount.deploymentTxHash, hash);
		fieldWithValue.put(qTonFeeAccount.modified, DateTimeUtil.currentEpochMilliSecondsUTC());
		long count = tonFeeAccountRepository.updateFields(queryPredicate, qTonFeeAccount, fieldWithValue);
		if (count < 1) {
			throw new BadRequestException(String.format(Errors.FEE_ACCOUNT_NOT_FOUND, rawAddress, chainId), null, null);
		}
		return DeployedAccountResponse.builder().transactionHash(hash).build();
	}

	@Override
	public List<TonAccountResponse> getFeeAccounts() {
		return getFeeAccountsInternal().stream()
				.map(feeAccount -> TonAccountResponse.builder().address(feeAccount.getAddress()).localBalance(feeAccount.getTonBalance()).build())
				.collect(Collectors.toList());
	}

	@Override
	public List<TonFeeAccount> getFeeAccountsInternal() {
		int chainId = ExecutionContextUtil.getContext().getChainId();
		Predicate queryPredicate = qTonFeeAccount.chainId.eq(chainId);
		return tonFeeAccountRepository.findAndProject(queryPredicate, qTonFeeAccount, qTonFeeAccount.address, qTonFeeAccount.publicKey,
				qTonFeeAccount.id, qTonFeeAccount.tonBalance, qTonFeeAccount.privateKey);
	}
}
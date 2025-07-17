package com.speed.toncore.accounts.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.speed.javacommon.exceptions.BadRequestException;
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
import com.speed.toncore.ton.TonNode;
import com.speed.toncore.ton.TonNodePool;
import com.speed.toncore.util.SecurityManagerUtil;
import com.speed.toncore.util.TonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.ton.ton4j.smartcontract.highload.HighloadWalletV3;
import org.ton.ton4j.utils.Utils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TonMainAccountServiceImpl implements TonMainAccountService {

	private static final QTonMainAccount qTonMainAccount = QTonMainAccount.tonMainAccount;
	private final TonMainAccountRepository tonMainAccountRepository;
	private final TonNodePool tonNodePool;
	private final TonCoreService tonCoreService;
	private final ApplicationEventPublisher applicationEventPublisher;

	@Override
	public TonAccountResponse createMainAccount(String tokenAddress) {
		TonNode tonNode = tonNodePool.getTonNodeByChainId();
		String encryptionAlgo = tonNode.getEncryptionAlgo();
		byte[] encryptionKey = tonNode.getEncryptionKey();
		HighloadWalletV3 mainAccountWallet = HighloadWalletV3.builder()
				.walletId(tonNode.getWalletId())
				.keyPair(Utils.generateSignatureKeyPair())
				.build();
		String encryptedPriKey = SecurityManagerUtil.encrypt(encryptionAlgo, Utils.bytesToHex(mainAccountWallet.getKeyPair().getSecretKey()),
				encryptionKey);
		TonMainAccount tonMainAccount = TonMainAccount.builder()
				.address(mainAccountWallet.getAddress().toRaw())
				.publicKey(Utils.bytesToHex(mainAccountWallet.getKeyPair().getPublicKey()))
				.privateKey(encryptedPriKey)
				.addressType(mainAccountWallet.getName())
				.tonBalance(BigDecimal.ZERO)
				.tokenAddress(tokenAddress)
				.chainId(tonNode.getChainId())
				.mainNet(tonNode.isMainNet())
				.build();
		tonMainAccountRepository.save(tonMainAccount);
		return TonAccountResponse.builder().address(tonMainAccount.getAddress()).build();
	}

	@Override
	public void addMainAccountContractAddress(String address) {
		String rawAddress = TonUtil.toRawAddress(address);
		TonNode tonNode = tonNodePool.getTonNodeByChainId();
		Predicate queryPredicate = new BooleanBuilder(qTonMainAccount.address.eq(rawAddress)).and(qTonMainAccount.chainId.eq(tonNode.getChainId()));
		TonMainAccount tonMainAccount = tonMainAccountRepository.findOne(queryPredicate)
				.orElseThrow(() -> new BadRequestException(String.format(Errors.MAIN_ACCOUNT_NOT_FOUND, rawAddress, tonNode.getChainId()), null, null));
		String tokenContractAddress = tonCoreService.getTokenContractAddress(tonMainAccount.getAddress(), tonMainAccount.getTokenAddress());
		if (StringUtil.nullOrEmpty(tokenContractAddress)) {
			throw new BadRequestException(String.format(Errors.TOKEN_CONTRACT_NOT_FOUND, address, tonMainAccount.getTokenAddress()), null, null);
		}
		Map<Path<?>, Object> fieldWithValue = HashMap.newHashMap(2);
		fieldWithValue.put(qTonMainAccount.tokenContractAddress, tokenContractAddress);
		fieldWithValue.put(qTonMainAccount.modified, DateTimeUtil.currentEpochMilliSecondsUTC());
		long count = tonMainAccountRepository.updateFields(queryPredicate, qTonMainAccount, fieldWithValue);
		if (count < 1) {
			throw new BadRequestException(String.format(Errors.MAIN_ACCOUNT_NOT_FOUND, address, tonNode.getChainId()), null, null);
		}
	}

	@Override
	public void deleteMainAccount(String address) {
		address = TonUtil.toRawAddress(address);
		Predicate queryPredicate = new BooleanBuilder(qTonMainAccount.address.eq(address)).and(
				qTonMainAccount.chainId.eq(ExecutionContextUtil.getContext().getChainId()));
		tonMainAccountRepository.deleteByPredicate(queryPredicate, qTonMainAccount);
	}

	@Override
	public DeployedAccountResponse deployMainAccount(String address) {
		String rawAddress = TonUtil.toRawAddress(address);
		TonNode tonNode = tonNodePool.getTonNodeByChainId();
		Predicate queryPredicate = new BooleanBuilder(qTonMainAccount.address.eq(address)).and(qTonMainAccount.chainId.eq(tonNode.getChainId()));
		TonMainAccount tonMainAccount = tonMainAccountRepository.findOne(queryPredicate)
				.orElseThrow(() -> new BadRequestException(String.format(Errors.MAIN_ACCOUNT_NOT_FOUND, rawAddress, tonNode.getChainId()), null, null));
		if (tonCoreService.isDeployed(rawAddress)) {
			throw new BadRequestException(String.format(Errors.ACCOUNT_ALREADY_DEPLOYED, rawAddress), null, null);
		}
		BigDecimal accountBalance = tonCoreService.fetchTonBalance(rawAddress);
		if (accountBalance.compareTo(BigDecimal.ZERO) <= 0) {
			throw new BadRequestException(String.format(Errors.NOT_ENOUGH_FUNDS_TO_DEPLOY, rawAddress), null, null);
		}
		String hash = tonCoreService.deployMainAccount(tonMainAccount.getPrivateKey());
		Map<Path<?>, Object> fieldWithValue = HashMap.newHashMap(2);
		fieldWithValue.put(qTonMainAccount.deploymentTxHash, hash);
		fieldWithValue.put(qTonMainAccount.modified, DateTimeUtil.currentEpochMilliSecondsUTC());
		long count = tonMainAccountRepository.updateFields(queryPredicate, qTonMainAccount, fieldWithValue);
		if (count < 1) {
			throw new BadRequestException(String.format(Errors.MAIN_ACCOUNT_NOT_FOUND, address, tonNode.getChainId()), null, null);
		}
		return DeployedAccountResponse.builder().transactionHash(hash).build();
	}

	@Override
	public List<TonAccountResponse> getMainAccounts(String tokenAddress) {
		List<TonMainAccount> mainAccounts = getMainAccountInternal(tokenAddress);
		return mainAccounts.stream()
				.map(mainAccount -> TonAccountResponse.builder().address(mainAccount.getAddress()).localBalance(mainAccount.getTonBalance()).build())
				.collect(Collectors.toList());
	}

	@Override
	public List<TonMainAccount> getMainAccountInternal(String tokenAddress) {
		tokenAddress = TonUtil.toRawAddress(tokenAddress);
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
		Predicate queryPredicate = qTonMainAccount.chainId.eq(chainId).and(qTonMainAccount.tokenAddress.eq(tokenAddress));
		return tonMainAccountRepository.findAndProject(queryPredicate, qTonMainAccount, qTonMainAccount.address, qTonMainAccount.publicKey,
				qTonMainAccount.id, qTonMainAccount.tonBalance, qTonMainAccount.privateKey, qTonMainAccount.tokenContractAddress);
	}
}
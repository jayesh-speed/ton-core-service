package com.speed.toncore.accounts.service.impl;

import com.iwebpp.crypto.TweetNaclFast;
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
import com.speed.toncore.ton.TonNode;
import com.speed.toncore.ton.TonNodePool;
import com.speed.toncore.util.SecurityManagerUtil;
import com.speed.toncore.util.TonUtils;
import lombok.RequiredArgsConstructor;
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
				.secretKey(encryptedPriKey)
				.walletId(tonNode.getWalletId())
				.walletType(feeAccount.getName())
				.chainId(tonNode.getChainId())
				.tonBalance(BigDecimal.ZERO)
				.mainNet(tonNode.isMainNet())
				.build();
		tonFeeAccountRepository.save(tonFeeAccount);
		return TonAccountResponse.builder().address(tonFeeAccount.getAddress()).build();
	}

	@Override
	public void deleteFeeAccount(String address) {
		address = TonUtils.toRawAddress(address);
		Predicate queryPredicate = getFeeAccountQueryPredicate(address);
		tonFeeAccountRepository.deleteByPredicate(queryPredicate, qTonFeeAccount);
	}

	@Override
	public DeployedAccountResponse deployFeeAccount(String address) {
		String rawAddress = TonUtils.toRawAddress(address);
		TonNode tonNode = tonNodePool.getTonNodeByChainId();
		Predicate queryPredicate = getFeeAccountQueryPredicate(rawAddress);
		TonFeeAccount tonFeeAccount = tonFeeAccountRepository.findOne(queryPredicate)
				.orElseThrow(() -> new BadRequestException(String.format(Errors.FEE_ACCOUNT_NOT_FOUND, rawAddress, tonNode.getChainId()), null, null));
		if (tonCoreService.isDeployed(rawAddress)) {
			throw new BadRequestException(String.format(Errors.ACCOUNT_ALREADY_DEPLOYED, rawAddress), null, null);
		}
		BigDecimal accountBalance = tonCoreService.fetchTonBalance(rawAddress);
		if (accountBalance.compareTo(BigDecimal.ZERO) <= 0) {
			throw new BadRequestException(String.format(Errors.NOT_ENOUGH_FUNDS_TO_DEPLOY, rawAddress), null, null);
		}
		String secretKey = tonFeeAccount.getSecretKey();
		String encryptionAlgo = tonNode.getEncryptionAlgo();
		byte[] encryptionKey = tonNode.getEncryptionKey();
		String decryptedPriKey = SecurityManagerUtil.decrypt(encryptionAlgo, secretKey, encryptionKey);
		TweetNaclFast.Signature.KeyPair keyPair = TweetNaclFast.Signature.keyPair_fromSecretKey(Utils.hexToSignedBytes(decryptedPriKey));
		WalletV3R2 feeAccountWallet = WalletV3R2.builder().walletId(tonNode.getWalletId()).keyPair(keyPair).build();
		String deploymentTransactionMessage = feeAccountWallet.prepareDeployMsg().toCell().toBase64();
		String hash = tonCoreService.sendMessageWithReturnHash(deploymentTransactionMessage);
		if (StringUtil.nullOrEmpty(hash)) {
			throw new InternalServerErrorException(String.format(Errors.ERROR_DEPLOY_FEE_ACCOUNT, rawAddress));
		}
		Long currentTime = DateTimeUtil.currentEpochMilliSecondsUTC();
		Map<Path<?>, Object> fieldWithValue = HashMap.newHashMap(2);
		fieldWithValue.put(qTonFeeAccount.deploymentTxHash, hash);
		fieldWithValue.put(qTonFeeAccount.modified, currentTime);
		long count = tonFeeAccountRepository.updateFields(queryPredicate, qTonFeeAccount, fieldWithValue);
		if (count < 1) {
			throw new BadRequestException(String.format(Errors.NO_FEE_ACCOUNT, rawAddress), null, null);
		}
		return DeployedAccountResponse.builder().transactionHash(hash).build();
	}

	@Override
	public List<TonAccountResponse> getFeeAccounts() {
		return getTonFeeAccounts().stream()
				.map(feeAccount -> TonAccountResponse.builder().address(feeAccount.getAddress()).localBalance(feeAccount.getTonBalance()).build())
				.collect(Collectors.toList());
	}

	@Override
	public List<TonFeeAccount> getTonFeeAccounts() {
		int chainId = ExecutionContextUtil.getContext().getChainId();
		Predicate queryPredicate = qTonFeeAccount.chainId.eq(chainId);
		return tonFeeAccountRepository.findAndProject(queryPredicate, qTonFeeAccount, qTonFeeAccount.address, qTonFeeAccount.publicKey,
				qTonFeeAccount.id, qTonFeeAccount.tonBalance, qTonFeeAccount.secretKey);
	}
}
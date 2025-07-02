package com.speed.toncore.accounts.service.impl;

import com.iwebpp.crypto.TweetNaclFast;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.speed.javacommon.exceptions.BadRequestException;
import com.speed.javacommon.exceptions.InternalServerErrorException;
import com.speed.javacommon.util.CollectionUtil;
import com.speed.javacommon.util.DateTimeUtil;
import com.speed.toncore.accounts.response.DeployedAccountResponse;
import com.speed.toncore.accounts.response.TonAccountResponse;
import com.speed.toncore.accounts.service.TonMainAccountService;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.domain.model.QTonMainAccount;
import com.speed.toncore.domain.model.TonMainAccount;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import com.speed.toncore.pojo.JettonWalletDto;
import com.speed.toncore.repository.TonMainAccountRepository;
import com.speed.toncore.ton.TonCoreService;
import com.speed.toncore.ton.TonCoreServiceHelper;
import com.speed.toncore.ton.TonNode;
import com.speed.toncore.ton.TonNodePool;
import com.speed.toncore.util.SecurityManagerUtil;
import com.speed.toncore.util.TonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ton.ton4j.address.Address;
import org.ton.ton4j.cell.Cell;
import org.ton.ton4j.cell.CellBuilder;
import org.ton.ton4j.smartcontract.highload.HighloadWalletV3;
import org.ton.ton4j.smartcontract.types.HighloadQueryId;
import org.ton.ton4j.smartcontract.types.HighloadV3Config;
import org.ton.ton4j.tlb.ExternalMessageInInfo;
import org.ton.ton4j.tlb.InternalMessageInfoRelaxed;
import org.ton.ton4j.tlb.Message;
import org.ton.ton4j.tlb.MessageRelaxed;
import org.ton.ton4j.tlb.MsgAddressIntStd;
import org.ton.ton4j.utils.Utils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class TonMainAccountServiceImpl implements TonMainAccountService {

	private static final QTonMainAccount qTonMainAccount = QTonMainAccount.tonMainAccount;
	private final TonMainAccountRepository tonMainAccountRepository;
	private final TonNodePool tonNodePool;
	private final TonCoreService tonCoreService;
	private final TonCoreServiceHelper tonCoreServiceHelper;

	@Override
	public TonAccountResponse createMainAccount(String jettonMasterAddress) {
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
				.secretKey(encryptedPriKey)
				.walletId(tonNode.getWalletId())
				.walletType(mainAccountWallet.getName())
				.tonBalance(BigDecimal.ZERO)
				.jettonMasterAddress(jettonMasterAddress)
				.chainId(tonNode.getChainId())
				.mainNet(tonNode.isMainNet())
				.build();
		tonMainAccountRepository.save(tonMainAccount);
		return TonAccountResponse.builder().address(tonMainAccount.getAddress()).build();
	}

	@Override
	public void addMainAccountJettonWallet(String address) {
		String rawAddress = TonUtils.toRawAddress(address);
		TonNode tonNode = tonNodePool.getTonNodeByChainId();
		Predicate queryPredicate = new BooleanBuilder(qTonMainAccount.address.eq(rawAddress)).and(qTonMainAccount.chainId.eq(tonNode.getChainId()));
		TonMainAccount tonMainAccount = tonMainAccountRepository.findOne(queryPredicate)
				.orElseThrow(() -> new BadRequestException(String.format(Errors.MAIN_ACCOUNT_NOT_FOUND, rawAddress, tonNode.getChainId()), null, null));
		JettonWalletDto jettonWalletDto = tonCoreServiceHelper.getJettonWallet(tonMainAccount.getAddress(), tonMainAccount.getJettonMasterAddress());
		List<JettonWalletDto.JettonWallet> jettonWallets = jettonWalletDto.getJettonWallets();
		if (CollectionUtil.nullOrEmpty(jettonWallets)) {
			throw new BadRequestException(String.format(Errors.JETTON_WALLET_NOT_FOUND, address, tonMainAccount.getJettonMasterAddress()), null, null);
		}
		Long currentTime = DateTimeUtil.currentEpochMilliSecondsUTC();
		String jettonWalletAddress = jettonWallets.getFirst().getAddress();
		Map<Path<?>, Object> fieldWithValue = HashMap.newHashMap(2);
		fieldWithValue.put(qTonMainAccount.jettonWalletAddress, jettonWalletAddress);
		fieldWithValue.put(qTonMainAccount.modified, currentTime);
		long count = tonMainAccountRepository.updateFields(queryPredicate, qTonMainAccount, fieldWithValue);
		if (count < 1) {
			throw new BadRequestException(String.format(Errors.MAIN_ACCOUNT_NOT_FOUND, address, tonNode.getChainId()), null, null);
		}
	}

	@Override
	public void deleteMainAccount(String address) {
		address = TonUtils.toRawAddress(address);
		Predicate queryPredicate = new BooleanBuilder(qTonMainAccount.address.eq(address)).and(
				qTonMainAccount.chainId.eq(ExecutionContextUtil.getContext().getChainId()));
		tonMainAccountRepository.deleteByPredicate(queryPredicate, qTonMainAccount);
	}

	@Override
	public DeployedAccountResponse deployMainAccount(String address) {
		String rawAddress = TonUtils.toRawAddress(address);
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
		String secretKey = tonMainAccount.getSecretKey();
		String encryptionAlgo = tonNode.getEncryptionAlgo();
		byte[] encryptionKey = tonNode.getEncryptionKey();
		String decryptedPriKey = SecurityManagerUtil.decrypt(encryptionAlgo, secretKey, encryptionKey);
		TweetNaclFast.Signature.KeyPair keyPair = TweetNaclFast.Signature.keyPair_fromSecretKey(Utils.hexToSignedBytes(decryptedPriKey));
		HighloadWalletV3 mainAccountWallet = HighloadWalletV3.builder().walletId(tonNode.getWalletId()).keyPair(keyPair).build();
		HighloadV3Config config = HighloadV3Config.builder().walletId(tonNode.getWalletId()).queryId(HighloadQueryId.fromSeqno(0).getQueryId()).build();
		String deployMainAccountMessage = createDeployMainAccountMessage(config, mainAccountWallet);
		String hash;
		try {
			hash = tonCoreService.sendMessageWithReturnHash(deployMainAccountMessage);
		} catch (RuntimeException e) {
			LOG.error(e.getMessage(), e);
			throw new InternalServerErrorException(String.format(Errors.ERROR_DEPLOY_MAIN_ACCOUNT, rawAddress, tonNode.getChainId()));
		}
		Long currentTime = DateTimeUtil.currentEpochMilliSecondsUTC();
		Map<Path<?>, Object> fieldWithValue = HashMap.newHashMap(2);
		fieldWithValue.put(qTonMainAccount.deploymentTxHash, hash);
		fieldWithValue.put(qTonMainAccount.modified, currentTime);
		long count = tonMainAccountRepository.updateFields(queryPredicate, qTonMainAccount, fieldWithValue);
		if (count < 1) {
			throw new BadRequestException(String.format(Errors.MAIN_ACCOUNT_NOT_FOUND, address, tonNode.getChainId()), null, null);
		}
		return DeployedAccountResponse.builder().transactionHash(hash).build();
	}

	@Override
	public List<TonAccountResponse> getMainAccounts(String jettonMasterAddress) {
		List<TonMainAccount> mainAccounts = getMainAccountDetail(jettonMasterAddress);
		return mainAccounts.stream()
				.map(mainAccount -> TonAccountResponse.builder().address(mainAccount.getAddress()).localBalance(mainAccount.getTonBalance()).build())
				.collect(Collectors.toList());
	}

	@Override
	public List<TonMainAccount> getMainAccountDetail(String jettonMasterAddress) {
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
		Predicate queryPredicate = qTonMainAccount.chainId.eq(chainId).and(qTonMainAccount.jettonMasterAddress.eq(jettonMasterAddress));
		return tonMainAccountRepository.findAndProject(queryPredicate, qTonMainAccount, qTonMainAccount.address, qTonMainAccount.publicKey,
				qTonMainAccount.id, qTonMainAccount.tonBalance, qTonMainAccount.secretKey, qTonMainAccount.jettonWalletAddress);
	}

	private String createDeployMainAccountMessage(HighloadV3Config config, HighloadWalletV3 wallet) {
		Address ownAddress = wallet.getAddress();
		if (isNull(config.getBody())) {
			config.setBody(MessageRelaxed.builder()
					.info(InternalMessageInfoRelaxed.builder()
							.dstAddr(MsgAddressIntStd.builder().workchainId(ownAddress.wc).address(ownAddress.toBigInteger()).build())
							.createdAt((config.getCreatedAt() == 0) ? Instant.now().getEpochSecond() - 60 : config.getCreatedAt())
							.build())
					.build()
					.toCell());
		}
		Cell innerMsg = wallet.createTransferMessage(config);
		TweetNaclFast.Signature.KeyPair keyPair = wallet.getKeyPair();
		Message externalMessage = Message.builder()
				.info(ExternalMessageInInfo.builder()
						.dstAddr(MsgAddressIntStd.builder().workchainId(ownAddress.wc).address(ownAddress.toBigInteger()).build())
						.build())
				.init(wallet.getStateInit())
				.body(CellBuilder.beginCell()
						.storeBytes(Utils.signData(keyPair.getPublicKey(), keyPair.getSecretKey(), innerMsg.hash()))
						.storeRef(innerMsg)
						.endCell())
				.build();
		return externalMessage.toCell().toBase64();
	}
}

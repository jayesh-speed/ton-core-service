package com.speed.toncore.accounts.service.impl;

import com.iwebpp.crypto.TweetNaclFast;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.speed.javacommon.exceptions.InternalServerErrorException;
import com.speed.toncore.accounts.request.TonWalletRequest;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.domain.model.QTonFeeAccount;
import com.speed.toncore.domain.model.QTonMainAccount;
import com.speed.toncore.domain.model.TonFeeAccount;
import com.speed.toncore.domain.model.TonMainAccount;
import com.speed.toncore.domain.model.TonWalletAddress;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import com.speed.toncore.repository.TonFeeAccountRepository;
import com.speed.toncore.repository.TonMainAccountRepository;
import com.speed.toncore.repository.TonWalletAddressRepository;
import com.speed.toncore.ton.TonCoreService;
import com.speed.toncore.ton.TonNode;
import com.speed.toncore.ton.TonNodePool;
import com.speed.toncore.util.SecurityManagerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.ton.ton4j.address.Address;
import org.ton.ton4j.cell.Cell;
import org.ton.ton4j.cell.CellBuilder;
import org.ton.ton4j.smartcontract.highload.HighloadWalletV3;
import org.ton.ton4j.smartcontract.types.HighloadQueryId;
import org.ton.ton4j.smartcontract.types.HighloadV3Config;
import org.ton.ton4j.smartcontract.wallet.v3.WalletV3R2;
import org.ton.ton4j.smartcontract.wallet.v5.WalletV5;
import org.ton.ton4j.tlb.ExternalMessageInInfo;
import org.ton.ton4j.tlb.InternalMessageInfoRelaxed;
import org.ton.ton4j.tlb.Message;
import org.ton.ton4j.tlb.MessageRelaxed;
import org.ton.ton4j.tlb.MsgAddressIntStd;
import org.ton.ton4j.utils.Utils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.isNull;

@Component
@Slf4j
@RequiredArgsConstructor
public class TonWalletServiceHelper {

	private final static QTonMainAccount qTonMainAccount = QTonMainAccount.tonMainAccount;
	private final static QTonFeeAccount qTonFeeAccount = QTonFeeAccount.tonFeeAccount;
	private final AtomicBoolean createInProgress = new AtomicBoolean(false);
	private final TonNodePool tonNodePool;
	private final TonWalletAddressRepository tonWalletAddressRepository;
	private final TonMainAccountRepository tonMainAccountRepository;
	private final TonFeeAccountRepository tonFeeAccountRepository;
	private final TonCoreService tonCoreService;

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public TonWalletAddress fetchWalletAddress() {
		long chainId = ExecutionContextUtil.getContext().getChainId();
		TonWalletAddress walletAddress = tonWalletAddressRepository.findFirstByChainIdOrderByIdAsc(chainId);
		tonWalletAddressRepository.delete(walletAddress);
		return walletAddress;
	}

	@Async
	public void createPoolOfWalletAddresses(TonWalletRequest tonWalletRequest) {
		if (createInProgress.get()) {
			LOG.warn(Errors.WARN_CREATE_IN_PROGRESS);
			return;
		}
		synchronized (this) {
			if (createInProgress.get()) {
				LOG.warn(Errors.WARN_CREATE_IN_PROGRESS);
				return;
			}
			createInProgress.getAndSet(true);
		}
		try {
			List<TonWalletAddress> tonAddresses = new ArrayList<>();
			int tonAddressesCount = tonWalletRequest.getCount();
			TonNode tonNode = tonNodePool.getTonNodeByChainId();
			String encryptionAlgo = tonNode.getEncryptionAlgo();
			byte[] encryptionKey = tonNode.getEncryptionKey();
			for (int i = 0; i < tonAddressesCount; i++) {
				WalletV5 wallet = generateTonWalletV5(tonNode);
				String encryptedPriKey = SecurityManagerUtil.encrypt(encryptionAlgo, Utils.bytesToHex(wallet.getKeyPair().getSecretKey()),
						encryptionKey);
				TonWalletAddress walletAddress = TonWalletAddress.builder()
						.address(wallet.getAddress().toRaw())
						.secretKey(encryptedPriKey)
						.publicKey(Utils.bytesToHex(wallet.getKeyPair().getPublicKey()))
						.walletType(wallet.getName())
						.walletId(tonNode.getWalletId())
						.chainId(tonNode.getChainId())
						.mainNet(tonNode.isMainNet())
						.build();
				tonAddresses.add(walletAddress);
			}
			tonWalletAddressRepository.saveAll(tonAddresses);
		} finally {
			createInProgress.set(false);
		}
	}

	public TonMainAccount createMainAccount(String jettonMasterAddress) {
		TonNode tonNode = tonNodePool.getTonNodeByChainId();
		String encryptionAlgo = tonNode.getEncryptionAlgo();
		byte[] encryptionKey = tonNode.getEncryptionKey();
		HighloadWalletV3 mainAccountWallet = HighloadWalletV3.builder().walletId(tonNode.getWalletId()).keyPair(generateKeyPair()).build();
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
		return tonMainAccount;
	}

	public TonFeeAccount createFeeAccount() {
		TonNode tonNode = tonNodePool.getTonNodeByChainId();
		String encryptionAlgo = tonNode.getEncryptionAlgo();
		byte[] encryptionKey = tonNode.getEncryptionKey();
		WalletV3R2 feeAccount = WalletV3R2.builder().walletId(tonNode.getWalletId()).keyPair(generateKeyPair()).build();
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
		return tonFeeAccount;
	}

	public String createDeployMainAccountMessage(String address) {
		TonNode tonNode = tonNodePool.getTonNodeByChainId();
		Predicate queryPredicate = new BooleanBuilder(qTonMainAccount.address.eq(address)).and(
				qTonMainAccount.chainId.eq(ExecutionContextUtil.getContext().getChainId()));
		TonMainAccount tonMainAccount = tonMainAccountRepository.findOne(queryPredicate)
				.orElseThrow(() -> new RuntimeException(Errors.MAIN_ACCOUNT_NOT_FOUND));
		if (tonCoreService.isDeployed(address)) {
			throw new InternalServerErrorException("Account already deployed");
		}
		BigDecimal accountBalance = tonCoreService.fetchTonBalance(address);
		if (accountBalance.compareTo(BigDecimal.ZERO) <= 0) {
			throw new InternalServerErrorException("Account balance is zero");
		}
		String secretKey = tonMainAccount.getSecretKey();
		String encryptionAlgo = tonNode.getEncryptionAlgo();
		byte[] encryptionKey = tonNode.getEncryptionKey();
		String decryptedPriKey = SecurityManagerUtil.decrypt(encryptionAlgo, secretKey, encryptionKey);
		TweetNaclFast.Signature.KeyPair keyPair = TweetNaclFast.Signature.keyPair_fromSecretKey(Utils.hexToSignedBytes(decryptedPriKey));

		HighloadWalletV3 mainAccountWallet = HighloadWalletV3.builder().walletId(tonNode.getWalletId()).keyPair(keyPair).build();

		HighloadV3Config config = HighloadV3Config.builder().walletId(tonNode.getWalletId()).queryId(HighloadQueryId.fromSeqno(0).getQueryId()).build();
		String deployMessage = createDeployMainAccountMessage(config, mainAccountWallet);
		return tonCoreService.sendMessageWithReturnHash(deployMessage);
	}

	public String deployFeeAccount(String address) {
		TonNode tonNode = tonNodePool.getTonNodeByChainId();
		Predicate queryPredicate = new BooleanBuilder(qTonFeeAccount.address.eq(address)).and(
				qTonFeeAccount.chainId.eq(ExecutionContextUtil.getContext().getChainId()));
		TonFeeAccount tonFeeAccount = tonFeeAccountRepository.findOne(queryPredicate)
				.orElseThrow(() -> new RuntimeException(Errors.FEE_ACCOUNT_NOT_FOUND));
		if (tonCoreService.isDeployed(address)) {
			throw new InternalServerErrorException("Account already deployed");
		}
		BigDecimal accountBalance = tonCoreService.fetchTonBalance(address);
		if (accountBalance.compareTo(BigDecimal.ZERO) <= 0) {
			throw new InternalServerErrorException("Account balance is zero");
		}
		String secretKey = tonFeeAccount.getSecretKey();
		String encryptionAlgo = tonNode.getEncryptionAlgo();
		byte[] encryptionKey = tonNode.getEncryptionKey();
		String decryptedPriKey = SecurityManagerUtil.decrypt(encryptionAlgo, secretKey, encryptionKey);
		TweetNaclFast.Signature.KeyPair keyPair = TweetNaclFast.Signature.keyPair_fromSecretKey(Utils.hexToSignedBytes(decryptedPriKey));
		WalletV3R2 feeAccountWallet = WalletV3R2.builder().walletId(tonNode.getWalletId()).keyPair(keyPair).build();
		String deployMessage = feeAccountWallet.prepareDeployMsg().toCell().toBase64();
		return tonCoreService.sendMessageWithReturnHash(deployMessage);
	}

	public String createDeployMainAccountMessage(HighloadV3Config config, HighloadWalletV3 wallet) {
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

	private WalletV5 generateTonWalletV5(TonNode tonNode) {
		return WalletV5.builder().walletId(tonNode.getWalletId()).keyPair(generateKeyPair()).isSigAuthAllowed(true).build();
	}

	private TweetNaclFast.Signature.KeyPair generateKeyPair() {
		return Utils.generateSignatureKeyPair();
	}
}

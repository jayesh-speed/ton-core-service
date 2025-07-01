package com.speed.toncore.accounts.service.impl;

import com.speed.toncore.accounts.request.TonWalletRequest;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.domain.model.TonWalletAddress;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import com.speed.toncore.repository.TonWalletAddressRepository;
import com.speed.toncore.ton.TonNode;
import com.speed.toncore.ton.TonNodePool;
import com.speed.toncore.util.SecurityManagerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.ton.ton4j.smartcontract.wallet.v5.WalletV5;
import org.ton.ton4j.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
@RequiredArgsConstructor
public class TonWalletServiceHelper {

	private final AtomicBoolean createInProgress = new AtomicBoolean(false);
	private final TonNodePool tonNodePool;
	private final TonWalletAddressRepository tonWalletAddressRepository;

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public TonWalletAddress fetchWalletAddress() {
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
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
				WalletV5 wallet = WalletV5.builder()
						.walletId(tonNode.getWalletId())
						.keyPair(Utils.generateSignatureKeyPair())
						.isSigAuthAllowed(true)
						.build();
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
}

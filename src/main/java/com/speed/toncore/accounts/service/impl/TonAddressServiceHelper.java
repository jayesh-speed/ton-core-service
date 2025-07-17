package com.speed.toncore.accounts.service.impl;

import com.speed.toncore.accounts.request.TonAddressRequest;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.domain.model.TonAddress;
import com.speed.toncore.events.TonAddressCreatedEvent;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import com.speed.toncore.repository.TonAddressRepository;
import com.speed.toncore.ton.TonNode;
import com.speed.toncore.ton.TonNodePool;
import com.speed.toncore.util.LogMessages;
import com.speed.toncore.util.SecurityManagerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
public class TonAddressServiceHelper {

	private final AtomicBoolean createInProgress = new AtomicBoolean(false);
	private final TonNodePool tonNodePool;
	private final TonAddressRepository tonAddressRepository;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public TonAddress fetchTonAddress() {
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
		TonAddress tonAddress = tonAddressRepository.findFirstByChainIdOrderByIdAsc(chainId);
		tonAddressRepository.delete(tonAddress);
		return tonAddress;
	}

	@Async
	public void createPoolOfTonAddresses(TonAddressRequest tonAddressRequest) {
		if (createInProgress.get()) {
			LOG.warn(LogMessages.Warn.WARN_CREATE_IN_PROGRESS);
			return;
		}
		synchronized (this) {
			if (createInProgress.get()) {
				LOG.warn(LogMessages.Warn.WARN_CREATE_IN_PROGRESS);
				return;
			}
			createInProgress.getAndSet(true);
		}
		try {
			List<TonAddress> tonAddresses = new ArrayList<>();
			int tonAddressesCount = tonAddressRequest.getCount();
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
				TonAddress walletAddress = TonAddress.builder()
						.address(wallet.getAddress().toRaw())
						.privateKey(encryptedPriKey)
						.publicKey(Utils.bytesToHex(wallet.getKeyPair().getPublicKey()))
						.addressType(wallet.getName())
						.chainId(tonNode.getChainId())
						.mainNet(tonNode.isMainNet())
						.build();
				tonAddresses.add(walletAddress);
			}
			tonAddressRepository.saveAll(tonAddresses);
			eventPublisher.publishEvent(new TonAddressCreatedEvent()); // Update the cache and refresh the receive addresses
		} catch (Exception e) {
			LOG.error(String.format(Errors.ERROR_WHILE_CREATING_WALLETS, e.getMessage()), e);
		} finally {
			createInProgress.set(false);
		}
	}
}

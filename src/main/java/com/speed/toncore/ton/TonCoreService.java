package com.speed.toncore.ton;

import com.iwebpp.crypto.TweetNaclFast;
import com.speed.javacommon.exceptions.InternalServerErrorException;
import com.speed.javacommon.log.LogHolder;
import com.speed.javacommon.util.CollectionUtil;
import com.speed.javacommon.util.StringUtil;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import com.speed.toncore.pojo.JettonWalletDto;
import com.speed.toncore.tokens.response.TonTokenResponse;
import com.speed.toncore.util.LogKeys;
import com.speed.toncore.util.LogMessages;
import com.speed.toncore.util.SecurityManagerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.marker.Markers;
import org.springframework.retry.RetryException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.ton.ton4j.address.Address;
import org.ton.ton4j.cell.Cell;
import org.ton.ton4j.cell.CellBuilder;
import org.ton.ton4j.smartcontract.SendMode;
import org.ton.ton4j.smartcontract.highload.HighloadWalletV3;
import org.ton.ton4j.smartcontract.token.ft.JettonWallet;
import org.ton.ton4j.smartcontract.types.Destination;
import org.ton.ton4j.smartcontract.types.HighloadQueryId;
import org.ton.ton4j.smartcontract.types.HighloadV3Config;
import org.ton.ton4j.smartcontract.types.WalletV3Config;
import org.ton.ton4j.smartcontract.types.WalletV5Config;
import org.ton.ton4j.smartcontract.utils.MsgUtils;
import org.ton.ton4j.smartcontract.wallet.v3.WalletV3R2;
import org.ton.ton4j.smartcontract.wallet.v5.WalletV5;
import org.ton.ton4j.tlb.ExternalMessageInInfo;
import org.ton.ton4j.tlb.InternalMessageInfoRelaxed;
import org.ton.ton4j.tlb.Message;
import org.ton.ton4j.tlb.MessageRelaxed;
import org.ton.ton4j.tlb.MsgAddressIntStd;
import org.ton.ton4j.utils.Utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.isNull;

@Component
@Slf4j
@RequiredArgsConstructor
public class TonCoreService {

	private final TonNodePool tonNodePool;
	private final TonCoreServiceHelper tonCoreServiceHelper;

	/**
	 * Generates a unique query ID based on the current system time.
	 * <p>
	 * The method uses the current system time in milliseconds modulo 1,000,000
	 * to ensure the resulting number:
	 * <ul>
	 *   <li>Is always positive</li>
	 *   <li>Stays within a range acceptable for the query ID (less than 8,380,415)</li>
	 *   <li>Rolls over roughly every 16 minutes, ensuring recent values are not reused</li>
	 * </ul>
	 * <p>
	 * This provides a simple and efficient way to generate short-lived,
	 * unique sequence numbers for use in query ID construction,
	 * while keeping them safely within expected bounds.
	 */
	private static synchronized int generateUniqueQueryId() {
		int uniqueNumber = (int) (System.currentTimeMillis() % 1_000_000);
		return HighloadQueryId.fromSeqno(uniqueNumber).getQueryId();
	}

	public BigDecimal fetchTonBalance(String address) {
		BigDecimal rawBalance = new BigDecimal(tonCoreServiceHelper.getTonBalance(address));
		if (rawBalance.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}
		return rawBalance.divide(BigDecimal.valueOf(Constants.ONE_BILLION), 9, RoundingMode.HALF_DOWN);
	}

	public BigDecimal fetchTokenBalance(String tokenAddress, String ownerAddress, int decimals) {
		return Optional.ofNullable(tonCoreServiceHelper.getJettonWallet(ownerAddress, tokenAddress).getJettonWallets())
				.flatMap(wallets -> wallets.stream().findFirst())
				.filter(jettonWallet -> !Objects.equals(jettonWallet.getBalance(), BigInteger.ZERO))
				.map(jettonWallet -> new BigDecimal(jettonWallet.getBalance()).divide(BigDecimal.TEN.pow(decimals), 9, RoundingMode.HALF_DOWN))
				.orElse(BigDecimal.ZERO);
	}

	public String getTokenContractAddress(String ownerAddress, String tokenAddress) {
		return Optional.ofNullable(tonCoreServiceHelper.getJettonWallet(ownerAddress, tokenAddress).getJettonWallets())
				.flatMap(wallets -> wallets.stream().findFirst())
				.map(JettonWalletDto.JettonWallet::getAddress)
				.orElse(null);
	}

	public boolean isDeployed(String address) {
		return tonCoreServiceHelper.isDeployed(address);
	}

	@Retryable(retryFor = RetryException.class, backoff = @Backoff(delay = 2000), maxAttempts = 5)
	public String transferTokens(String fromAddress, String toAddress, TonTokenResponse token, BigDecimal value, String encryptedKey,
			String tokenContractAddress, String txReference, BigInteger transactionFee) {
		try {
			TonNode tonNode = tonNodePool.getTonNodeByChainId();
			BigDecimal scaled = value.setScale(token.getDecimals(), RoundingMode.HALF_DOWN);
			BigInteger amountToTransfer = scaled.multiply(BigDecimal.valueOf(Math.pow(10, token.getDecimals()))).toBigInteger();
			String privateKey = getDecryptedKey(encryptedKey, tonNode);
			if (StringUtil.nullOrEmpty(privateKey)) {
				LogHolder logHolder = prepareTokenTransferFailLogHolder(fromAddress, toAddress, token.getTokenAddress(), value);
				LOG.error(Markers.appendEntries(logHolder.getAttributes()), null);
				throw new InternalServerErrorException(Errors.PRIVATE_KEY_NOT_DECRYPTED);
			}
			HighloadWalletV3 mainAccount = HighloadWalletV3.builder().walletId(tonNode.getWalletId()).keyPair(getKeyPair(privateKey)).build();
			Address mainAccountAddress = mainAccount.getAddress();
			int queryId = generateUniqueQueryId();
			Cell forwardPayload = MsgUtils.createTextMessageBody(txReference);
			HighloadV3Config config = HighloadV3Config.builder()
					.walletId(tonNode.getWalletId())
					.queryId(queryId)
					.body(mainAccount.createBulkTransfer(Collections.singletonList(Destination.builder()
							.address(tokenContractAddress)
							.amount(transactionFee)
							.bounce(true)
							.body(JettonWallet.createTransferBody(queryId, amountToTransfer, Address.of(toAddress), mainAccountAddress, null,
									BigInteger.ONE, forwardPayload))
							.build()), BigInteger.valueOf(queryId)))
					.sendMode(SendMode.PAY_GAS_SEPARATELY_AND_IGNORE_ERRORS)
					.build();
			String bocMessage = createTokenTransferBocMessage(mainAccountAddress, mainAccount, config);
			try {
				return tonCoreServiceHelper.sendMessageWithReturnHash(bocMessage);
			} catch (RuntimeException e) {
				LogHolder logHolder = prepareTokenTransferFailLogHolder(fromAddress, toAddress, token.getTokenAddress(), value);
				LOG.error(Markers.appendEntries(logHolder.getAttributes()), null, e);
				if (shouldRetryTokenSend(e.getMessage().toLowerCase())) {
					throw new RetryException(String.format(Errors.ERROR_ON_CHAIN_RAW_TX, tonNode.getChainId()));
				}
				throw new InternalServerErrorException(String.format(Errors.ERROR_ON_CHAIN_RAW_TX, tonNode.getChainId()));
			}
		} catch (RetryException | InternalServerErrorException ex) {
			throw ex;
		} catch (Exception ex) {
			LogHolder logHolder = prepareTokenTransferFailLogHolder(fromAddress, toAddress, token.getTokenAddress(), value);
			LOG.error(Markers.appendEntries(logHolder.getAttributes()), null, ex);
			throw new InternalServerErrorException(String.format(Errors.ERROR_TOKEN_TRANSFER, token.getTokenAddress(), fromAddress, toAddress));
		}
	}

	private boolean shouldRetryTokenSend(String message) {
		return message.contains(Errors.TonIndexer.EXIT_CODE_33) || message.contains(Errors.TonIndexer.TOO_MANY_EXTERNAL_MESSAGE) ||
				message.contains(Errors.TonIndexer.UNPACK_ACCOUNT_STATE);
	}

	private String createTokenTransferBocMessage(Address ownAddress, HighloadWalletV3 wallet, HighloadV3Config config) {
		Cell body = wallet.createTransferMessage(config);
		TweetNaclFast.Signature.KeyPair keyPair = wallet.getKeyPair();
		Message externalMessage = Message.builder()
				.info(ExternalMessageInInfo.builder()
						.dstAddr(MsgAddressIntStd.builder().workchainId(ownAddress.wc).address(ownAddress.toBigInteger()).build())
						.build())
				.body(CellBuilder.beginCell()
						.storeBytes(Utils.signData(keyPair.getPublicKey(), keyPair.getSecretKey(), body.hash()))
						.storeRef(body)
						.endCell())
				.build();
		return externalMessage.toCell().toBase64();
	}

	@Retryable(retryFor = RetryException.class, backoff = @Backoff(delay = 2000), maxAttempts = 5)
	public String transferTokenToMainAccount(String spenderRawAddress, String encryptedKey, String tokenAddress, String mainAccountAddress,
			String feeAccountEncryptedKey, String txReference, BigInteger fee) {
		try {
			TonNode tonNode = tonNodePool.getTonNodeByChainId();
			String feeAccountPrivateKey = getDecryptedKey(feeAccountEncryptedKey, tonNode);
			String spenderAccountPrivateKey = getDecryptedKey(encryptedKey, tonNode);

			if (StringUtil.nullOrEmpty(feeAccountPrivateKey) || StringUtil.nullOrEmpty(spenderAccountPrivateKey)) {
				LogHolder logHolder = prepareSweepFailLogHolder(spenderRawAddress, mainAccountAddress, tokenAddress);
				LOG.error(Markers.appendEntries(logHolder.getAttributes()), null);
				throw new InternalServerErrorException(String.format(Errors.PRIVATE_KEY_NOT_DECRYPTED));
			}

			TweetNaclFast.Signature.KeyPair feeAccountKeyPair = getKeyPair(feeAccountPrivateKey);
			TweetNaclFast.Signature.KeyPair spenderAccountKeyPair = getKeyPair(spenderAccountPrivateKey);
			WalletV3R2 feeWalletContract = WalletV3R2.builder().walletId(tonNode.getWalletId()).keyPair(feeAccountKeyPair).build();
			WalletV5 spenderAccount = WalletV5.builder().walletId(tonNode.getWalletId()).keyPair(spenderAccountKeyPair).isSigAuthAllowed(true).build();

			Address spenderAddress = spenderAccount.getAddress();
			Address feeAccountAddress = feeWalletContract.getAddress();
			String feeAccountRawAddress = feeAccountAddress.toRaw();
			List<JettonWalletDto.JettonWallet> jettonWallets = tonCoreServiceHelper.getJettonWallet(spenderRawAddress, tokenAddress).getJettonWallets();
			if (CollectionUtil.nullOrEmpty(jettonWallets)) {
				throw new RetryException(String.format(Errors.RETRYING_TO_FETCH_TOKEN_CONTRACT, spenderAddress, tokenAddress));
			}
			JettonWalletDto.JettonWallet jettonWallet = jettonWallets.getFirst();
			if (jettonWallet.getBalance().compareTo(BigInteger.ZERO) == 0) {
				LOG.warn(String.format(LogMessages.Warn.ZERO_ACCOUNT_BALANCE, spenderRawAddress, tokenAddress));
				return null;
			}
			boolean isDeployed = tonCoreServiceHelper.isDeployed(spenderRawAddress);
			int seqNumber = tonCoreServiceHelper.getSeqNo(feeAccountRawAddress);
			WalletV5Config spenderConfig = createSweepTokenTransferConfig(isDeployed, spenderAccount, Address.of(mainAccountAddress), feeAccountAddress,
					tonNode, jettonWallet.getAddress(), jettonWallet.getBalance(), txReference);
			WalletV3Config config = WalletV3Config.builder()
					.seqno(seqNumber)
					.destination(spenderAddress)
					.walletId(tonNode.getWalletId())
					.sendMode(SendMode.PAY_GAS_SEPARATELY_AND_IGNORE_ERRORS)
					.bounce(true)
					.amount(fee)
					.body(spenderAccount.createInternalSignedBody(spenderConfig))
					.build();
			if (!isDeployed) {
				config.setStateInit(spenderAccount.getStateInit());
			}
			String message = feeWalletContract.prepareExternalMsg(config).toCell().toBase64();
			String hash;
			try {
				hash = tonCoreServiceHelper.sendMessageWithReturnHash(message);
			} catch (RuntimeException e) {
				LogHolder logHolder = prepareSweepFailLogHolder(spenderRawAddress, mainAccountAddress, tokenAddress);
				LOG.error(Markers.appendEntries(logHolder.getAttributes()), null);
				if (e.getMessage().contains(Errors.TonIndexer.UNPACK_ACCOUNT_STATE) || e.getMessage().contains(Errors.TonIndexer.EXIT_CODE_33)) {
					throw new RetryException(String.format(Errors.ERROR_FUND_TRANSFER_TO_MAIN_ACCOUNT, spenderRawAddress), e);
				}
				throw new InternalServerErrorException(String.format(Errors.ERROR_FUND_TRANSFER_TO_MAIN_ACCOUNT, spenderRawAddress), e);
			}
			LOG.info(String.format(LogMessages.Info.TRANSACTION_HASH, hash));
			return hash;
		} catch (RetryException | InternalServerErrorException ex) {
			throw ex;
		} catch (Exception ex) {
			LogHolder logHolder = prepareSweepFailLogHolder(spenderRawAddress, mainAccountAddress, tokenAddress);
			LOG.error(Markers.appendEntries(logHolder.getAttributes()), null);
			throw new InternalServerErrorException(String.format(Errors.ERROR_ON_SWEEP_TRANSACTION, ExecutionContextUtil.getContext().getChainId()), ex);
		}
	}

	private WalletV5Config createSweepTokenTransferConfig(boolean isDeployed, WalletV5 spenderAccount, Address mainAccountAddress,
			Address feeAccountAddress, TonNode tonNode, String spenderTokenContractAddress, BigInteger balance, String txReference) {
		int spenderSeqNo = 0;
		if (isDeployed) {
			spenderSeqNo = tonCoreServiceHelper.getSeqNo(spenderAccount.getAddress().toRaw());
		}
		Cell forwardPayload = MsgUtils.createTextMessageBody(txReference);
		return WalletV5Config.builder()
				.walletId(tonNode.getWalletId())
				.seqno(spenderSeqNo)
				.body(spenderAccount.createBulkTransfer(Collections.singletonList(Destination.builder()
						.bounce(true)
						.address(spenderTokenContractAddress)
						.sendMode(SendMode.CARRY_ALL_REMAINING_BALANCE)
						.body(JettonWallet.createTransferBody(generateUniqueQueryId(), balance, mainAccountAddress, feeAccountAddress, null,
								BigInteger.ONE, forwardPayload))
						.build())).toCell())
				.build();
	}

	public String deployFeeAccount(String encryptedKey) {
		TonNode tonNode = tonNodePool.getTonNodeByChainId();
		String privateKey = getDecryptedKey(encryptedKey, tonNode);
		if (StringUtil.nullOrEmpty(privateKey)) {
			throw new InternalServerErrorException(String.format(Errors.PRIVATE_KEY_NOT_DECRYPTED));
		}
		TweetNaclFast.Signature.KeyPair keyPair = getKeyPair(privateKey);
		WalletV3R2 feeAccountWallet = WalletV3R2.builder().walletId(tonNode.getWalletId()).keyPair(keyPair).build();
		String deploymentTransactionMessage = feeAccountWallet.prepareDeployMsg().toCell().toBase64();
		try {
			return tonCoreServiceHelper.sendMessageWithReturnHash(deploymentTransactionMessage);
		} catch (RuntimeException e) {
			LOG.error(e.getMessage(), e);
			throw new InternalServerErrorException(
					String.format(Errors.ERROR_DEPLOY_FEE_ACCOUNT, feeAccountWallet.getAddress().toRaw(), tonNode.getChainId()));
		}
	}

	public String deployMainAccount(String encryptedKey) {
		TonNode tonNode = tonNodePool.getTonNodeByChainId();
		String privateKey = getDecryptedKey(encryptedKey, tonNode);
		if (StringUtil.nullOrEmpty(privateKey)) {
			throw new InternalServerErrorException(String.format(Errors.PRIVATE_KEY_NOT_DECRYPTED));
		}
		TweetNaclFast.Signature.KeyPair keyPair = getKeyPair(privateKey);
		HighloadWalletV3 mainAccountWallet = HighloadWalletV3.builder().walletId(tonNode.getWalletId()).keyPair(keyPair).build();
		HighloadV3Config config = HighloadV3Config.builder().walletId(tonNode.getWalletId()).queryId(HighloadQueryId.fromSeqno(0).getQueryId()).build();
		String deployMainAccountMessage = createDeployMainAccountMessage(config, mainAccountWallet);
		try {
			return tonCoreServiceHelper.sendMessageWithReturnHash(deployMainAccountMessage);
		} catch (RuntimeException e) {
			LOG.error(e.getMessage(), e);
			throw new InternalServerErrorException(
					String.format(Errors.ERROR_DEPLOY_MAIN_ACCOUNT, mainAccountWallet.getAddress().toRaw(), tonNode.getChainId()));
		}
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

	private String getDecryptedKey(String encryptedKey, TonNode tonNode) {
		return SecurityManagerUtil.decrypt(tonNode.getEncryptionAlgo(), encryptedKey, tonNode.getEncryptionKey());
	}

	private TweetNaclFast.Signature.KeyPair getKeyPair(String privateKey) {
		return TweetNaclFast.Signature.keyPair_fromSecretKey(Utils.hexToSignedBytes(privateKey));
	}

	private LogHolder prepareTokenTransferFailLogHolder(String fromAddress, String toAddress, String tokenAddress, BigDecimal value) {
		LogHolder logHolder = new LogHolder();
		logHolder.put(LogKeys.FROM_ADDRESS, fromAddress);
		logHolder.put(LogKeys.TO_ADDRESS, toAddress);
		logHolder.put(LogKeys.TOKEN_ADDRESS, tokenAddress);
		logHolder.put(LogKeys.CHAIN_ID, ExecutionContextUtil.getContext().getChainId());
		logHolder.put(LogKeys.VALUE, value);
		return logHolder;
	}

	private LogHolder prepareSweepFailLogHolder(String spenderAddress, String mainAccountAddress, String tokenAddress) {
		LogHolder logHolder = new LogHolder();
		logHolder.put(LogKeys.SPENDER_ADDRESS, spenderAddress);
		logHolder.put(LogKeys.MAIN_ACCOUNT_ADDRESS, mainAccountAddress);
		logHolder.put(LogKeys.TOKEN_ADDRESS, tokenAddress);
		logHolder.put(LogKeys.CHAIN_ID, ExecutionContextUtil.getContext().getChainId());
		return logHolder;
	}
}

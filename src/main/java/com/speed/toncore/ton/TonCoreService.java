package com.speed.toncore.ton;

import com.iwebpp.crypto.TweetNaclFast;
import com.speed.javacommon.enums.CommonLogActions;
import com.speed.javacommon.exceptions.InternalServerErrorException;
import com.speed.javacommon.log.LogHolder;
import com.speed.javacommon.util.CollectionUtil;
import com.speed.javacommon.util.StringUtil;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import com.speed.toncore.pojo.JettonWalletDto;
import com.speed.toncore.pojo.TonTransactionDto;
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
import org.ton.ton4j.tlb.Message;
import org.ton.ton4j.tlb.MsgAddressIntStd;
import org.ton.ton4j.utils.Utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
		return new BigDecimal(tonCoreServiceHelper.getTonBalance(address)).divide(BigDecimal.valueOf(Math.pow(10, 9)));
	}

	public BigDecimal fetchJettonBalance(String jettonAddress, String address, int decimals) {
		return Optional.ofNullable(tonCoreServiceHelper.getJettonWallet(address, jettonAddress).getJettonWallets())
				.flatMap(wallets -> wallets.stream().findFirst())
				.map(jettonWallet -> new BigDecimal(jettonWallet.getBalance()).divide(BigDecimal.TEN.pow(decimals)))
				.orElse(BigDecimal.ZERO);
	}

	public boolean isDeployed(String address) {
		return tonCoreServiceHelper.isDeployed(address);
	}

	@Retryable(retryFor = RetryException.class, backoff = @Backoff(delay = 2000), maxAttempts = 5)
	public String transferJettons(String fromAddress, String toAddress, String jettonAddress, BigDecimal value, String encryptedKey, String identifier,
			String jettonWalletAddress, int decimals) {
		try {
			TonNode tonNode = tonNodePool.getTonNodeByChainId();
			BigDecimal scaled = value.setScale(decimals);
			BigInteger amountToTransfer = scaled.multiply(BigDecimal.valueOf(Math.pow(10, decimals))).toBigInteger();
			String privateKey = getDecryptedKey(encryptedKey, tonNode.getEncryptionAlgo(), tonNode.getEncryptionKey());
			if (StringUtil.nullOrEmpty(privateKey)) {
				LogHolder logHolder = prepareJettonTransferFailLogHolder(fromAddress, toAddress, jettonAddress, value);
				LOG.error(Markers.appendEntries(logHolder.getAttributes()), null);
				throw new InternalServerErrorException(String.format(Errors.PRIVATE_KEY_NOT_DECRYPTED, tonNode.getChainId()));
			}
			HighloadWalletV3 mainAccount = HighloadWalletV3.builder().walletId(tonNode.getWalletId()).keyPair(getKeyPair(privateKey)).build();
			Address address = mainAccount.getAddress();
			int queryId = generateUniqueQueryId();
			Cell textMessageBody = MsgUtils.createTextMessageBody(identifier);
			HighloadV3Config config = HighloadV3Config.builder()
					.walletId(tonNode.getWalletId())
					.queryId(queryId)
					.body(mainAccount.createBulkTransfer(Collections.singletonList(Destination.builder()
							.address(jettonWalletAddress)
							.amount(Utils.toNano(Constants.FORWARD_TON_AMOUNT_FOR_JETTON_TRANSFER, 9))
							.body(JettonWallet.createTransferBody(generateUniqueQueryId(), amountToTransfer, Address.of(toAddress), address, null,
									BigInteger.ONE, textMessageBody))
							.build()), BigInteger.valueOf(queryId)))
					.sendMode(SendMode.PAY_GAS_SEPARATELY_AND_IGNORE_ERRORS)
					.build();
			String message = createJettonTransferBocMessage(address.toRaw(), mainAccount, config);
			String hash;
			try {
				hash = tonCoreServiceHelper.sendMessageWithReturnHash(message);
				if (StringUtil.nullOrEmpty(hash)) {
					LogHolder logHolder = prepareJettonTransferFailLogHolder(fromAddress, toAddress, jettonAddress, value);
					LOG.error(Markers.appendEntries(logHolder.getAttributes()), null);
					throw new InternalServerErrorException(String.format(Errors.ERROR_ON_CHAIN_RAW_TX, tonNode.getChainId()));
				}
				return hash;
			} catch (RuntimeException e) {
				LogHolder logHolder = prepareJettonTransferFailLogHolder(fromAddress, toAddress, jettonAddress, value);
				LOG.error(Markers.appendEntries(logHolder.getAttributes()), null, e);
				if (shouldRetryJettonSend(e.getMessage().toLowerCase())) {
					throw new RetryException(String.format(Errors.ERROR_ON_CHAIN_RAW_TX, tonNode.getChainId()));
				}
				throw new InternalServerErrorException(String.format(Errors.ERROR_ON_CHAIN_RAW_TX, tonNode.getChainId()));
			}
		} catch (RetryException ex) {
			throw ex;
		} catch (Exception ex) {
			LogHolder logHolder = prepareJettonTransferFailLogHolder(fromAddress, toAddress, jettonAddress, value);
			LOG.error(Markers.appendEntries(logHolder.getAttributes()), null, ex);
			throw new InternalServerErrorException(String.format(Errors.ERROR_JETTON_TRANSFER, jettonAddress, fromAddress, toAddress));
		}
	}

	private boolean shouldRetryJettonSend(String message) {
		return message.contains(Constants.EXIT_CODE_33) || message.contains(Constants.TO_MANY_EXTERNAL_MASSAGE) ||
				message.contains(Constants.UNPACK_ACCOUNT_STATE);
	}

	private String createJettonTransferBocMessage(String mainAccountAddress, HighloadWalletV3 wallet, HighloadV3Config config) {
		Address ownAddress = Address.of(mainAccountAddress);
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
	public String transferJettonToMainAccount(String spenderRawAddress, String encryptedKey, String jettonMasterAddress, String mainAccountAddress,
			String feeAccountEncryptedKey, String txReference) {
		try {
			TonNode tonNode = tonNodePool.getTonNodeByChainId();
			String encryptionAlgo = tonNode.getEncryptionAlgo();
			byte[] encryptionKey = tonNode.getEncryptionKey();
			String feeAccountPrivateKey = getDecryptedKey(feeAccountEncryptedKey, encryptionAlgo, encryptionKey);
			String spenderAccountPrivateKey = getDecryptedKey(encryptedKey, encryptionAlgo, encryptionKey);

			if (StringUtil.nullOrEmpty(feeAccountPrivateKey) || StringUtil.nullOrEmpty(spenderAccountPrivateKey)) {
				LogHolder logHolder = prepareSweepFailLogHolder(spenderRawAddress, mainAccountAddress, jettonMasterAddress);
				LOG.error(Markers.appendEntries(logHolder.getAttributes()), null);
				throw new InternalServerErrorException(String.format(Errors.PRIVATE_KEY_NOT_DECRYPTED));
			}

			TweetNaclFast.Signature.KeyPair feeAccountKeyPair = getKeyPair(feeAccountPrivateKey);
			TweetNaclFast.Signature.KeyPair spenderAccountKeyPair = getKeyPair(spenderAccountPrivateKey);
			WalletV3R2 feeWalletContract = WalletV3R2.builder().walletId(tonNode.getWalletId()).keyPair(feeAccountKeyPair).build();
			WalletV5 spenderWalletContract = WalletV5.builder()
					.walletId(tonNode.getWalletId())
					.keyPair(spenderAccountKeyPair)
					.isSigAuthAllowed(true)
					.build();

			Address spenderAccountAddress = spenderWalletContract.getAddress();
			Address feeAccountAddress = feeWalletContract.getAddress();
			String feeAccountRawAddress = feeAccountAddress.toRaw();
			LOG.info(LogMessages.Info.WAITING_FOR_BALANCE_UPDATE, spenderRawAddress);
			Thread.sleep(5000);
			List<JettonWalletDto.JettonWallet> jettonWallets = tonCoreServiceHelper.getJettonWallet(spenderRawAddress, jettonMasterAddress)
					.getJettonWallets();
			if (CollectionUtil.nullOrEmpty(jettonWallets)) {
				LOG.error(String.format(Errors.ERROR_FETCHING_JETTON_WALLET, spenderRawAddress, jettonMasterAddress));
				throw new RetryException(String.format(Errors.RETRYING_TO_FETCH_JETTON_WALLET, spenderAccountAddress, jettonMasterAddress));
			}
			JettonWalletDto.JettonWallet jettonWallet = jettonWallets.getFirst();
			if (jettonWallet.getBalance().compareTo(BigInteger.ZERO) == 0) {
				LOG.warn(String.format(LogMessages.Warn.ZERO_ACCOUNT_BALANCE, spenderRawAddress, jettonMasterAddress));
				return null;
			}
			boolean isDeployed = tonCoreServiceHelper.isDeployed(spenderRawAddress);
			int seqNumber = tonCoreServiceHelper.getSeqNo(feeAccountRawAddress);
			WalletV5Config spenderConfig = createSweepJettonTransferConfig(isDeployed, spenderWalletContract, Address.of(mainAccountAddress),
					feeAccountAddress, tonNode, jettonWallet.getAddress(), jettonWallet.getBalance(), txReference);
			WalletV3Config config = WalletV3Config.builder()
					.seqno(seqNumber)
					.destination(spenderAccountAddress)
					.walletId(tonNode.getWalletId())
					.sendMode(SendMode.PAY_GAS_SEPARATELY_AND_IGNORE_ERRORS)
					.bounce(true)
					.amount(Utils.toNano(Constants.FORWARD_TON_AMOUNT_FOR_DEPLOYMENT))
					.body(spenderWalletContract.createInternalSignedBody(spenderConfig))
					.build();
			if (!isDeployed) {
				config.setStateInit(spenderWalletContract.getStateInit());
			}
			String message = feeWalletContract.prepareExternalMsg(config).toCell().toBase64();
			String hash;
			try {
				hash = tonCoreServiceHelper.sendMessageWithReturnHash("Hello Fail"+message);
			} catch (RuntimeException e) {
				LogHolder logHolder = prepareSweepFailLogHolder(spenderRawAddress, mainAccountAddress, jettonMasterAddress);
				LOG.error(Markers.appendEntries(logHolder.getAttributes()), null);
				if (e.getMessage().contains(Constants.UNPACK_ACCOUNT_STATE) || e.getMessage().contains(Constants.EXIT_CODE_33)) {
					throw new RetryException(String.format(Errors.ERROR_FUND_TRANSFER_TO_MAIN_ACCOUNT, spenderRawAddress), e);
				}
				throw new InternalServerErrorException(String.format(Errors.ERROR_FUND_TRANSFER_TO_MAIN_ACCOUNT, spenderRawAddress), e);
			}
			LOG.info(String.format(LogMessages.Info.TRANSACTION_HASH, hash));
			return hash;
		} catch (RetryException | InternalServerErrorException ex) {
			throw ex;
		} catch (Exception ex) {
			LogHolder logHolder = prepareSweepFailLogHolder(spenderRawAddress, mainAccountAddress, jettonMasterAddress);
			LOG.error(Markers.appendEntries(logHolder.getAttributes()), null);
			throw new InternalServerErrorException(String.format(Errors.ERROR_ON_SWEEP_TRANSACTION, ExecutionContextUtil.getContext().getChainId()), ex);
		}
	}

	private WalletV5Config createSweepJettonTransferConfig(boolean isDeployed, WalletV5 spenderWalletContract, Address mainAccountAddress,
			Address feeAccountAddress, TonNode tonNode, String spenderJettonWalletAddress, BigInteger balance, String identifier) {
		int spenderSeqNo = 0;
		if (isDeployed) {
			spenderSeqNo = tonCoreServiceHelper.getSeqNo(spenderWalletContract.getAddress().toRaw());
		}
		Cell textMessageBody = MsgUtils.createTextMessageBody(identifier);
		return WalletV5Config.builder()
				.walletId(tonNode.getWalletId())
				.seqno(spenderSeqNo)
				.body(spenderWalletContract.createBulkTransfer(Collections.singletonList(Destination.builder()
						.bounce(true)
						.address(spenderJettonWalletAddress)
						.sendMode(SendMode.CARRY_ALL_REMAINING_BALANCE)
						.body(JettonWallet.createTransferBody(0, balance, mainAccountAddress, feeAccountAddress, null, BigInteger.ONE, textMessageBody))
						.build())).toCell())
				.build();
	}

	private String getDecryptedKey(String encryptedKey, String encryptionAlgo, byte[] encryptionKey) {
		return SecurityManagerUtil.decrypt(encryptionAlgo, encryptedKey, encryptionKey);
	}

	private TweetNaclFast.Signature.KeyPair getKeyPair(String privateKey) {
		return TweetNaclFast.Signature.keyPair_fromSecretKey(Utils.hexToSignedBytes(privateKey));
	}

	private LogHolder prepareJettonTransferFailLogHolder(String fromAddress, String toAddress, String jettonAddress, BigDecimal value) {
		LogHolder logHolder = new LogHolder();
		logHolder.setAction(CommonLogActions.API_EXCEPTION);
		logHolder.put(Constants.FROM_ADDRESS, fromAddress);
		logHolder.put(Constants.TO_ADDRESS, toAddress);
		logHolder.put(Constants.JETTON_ADDRESS, jettonAddress);
		logHolder.put(Constants.CHAIN_ID, ExecutionContextUtil.getContext().getChainId());
		logHolder.put(Constants.VALUE, value);
		return logHolder;
	}

	private LogHolder prepareSweepFailLogHolder(String spenderAddress, String mainAccountAddress, String jettonAddress) {
		LogHolder logHolder = new LogHolder();
		logHolder.setAction(CommonLogActions.API_EXCEPTION);
		logHolder.put(Constants.SPENDER_ADDRESS, spenderAddress);
		logHolder.put(Constants.MAIN_ACCOUNT_ADDRESS, mainAccountAddress);
		logHolder.put(Constants.JETTON_ADDRESS, jettonAddress);
		logHolder.put(Constants.CHAIN_ID, ExecutionContextUtil.getContext().getChainId());
		return logHolder;
	}

	public String sendMessageWithReturnHash(String message) {
		return tonCoreServiceHelper.sendMessageWithReturnHash(message);
	}

	public TonTransactionDto getTransactionByMessageHash(String messageHash) {
		return tonCoreServiceHelper.getTransactionByMessageHash(messageHash);
	}
}

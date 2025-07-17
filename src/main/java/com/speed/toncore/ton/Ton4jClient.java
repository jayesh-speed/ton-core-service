package com.speed.toncore.ton;

import com.speed.javacommon.util.RequestIdGenerator;
import com.speed.toncore.accounts.service.TonAddressService;
import com.speed.toncore.aws.SecretManagerService;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import com.speed.toncore.listener.service.impl.ListenerServiceImpl;
import com.speed.toncore.schedular.TonConfigParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class Ton4jClient {

	private final TonNodePool tonNodePool;
	private final SecretManagerService secretManagerService;
	private final ListenerServiceImpl listenerService;
	private final TonAddressService tonAddressService;
	private final TonConfigParam tonConfigParam;

	public void setupTonNodes() {
		List<Integer> supportedChainIds = tonNodePool.getSupportedChainIds();
		supportedChainIds.forEach(chainId -> {
			initContext(chainId);
			TonNode tonNode = setupNode(chainId);
			tonNodePool.addTonNodeAndChainId(chainId, tonNode);
			Set<String> receivingAddresses = tonAddressService.fetchReceiveAddresses(chainId);
			Set<String> sendingAddresses = tonAddressService.fetchSendAddresses(chainId);
			tonConfigParam.initConfigParam(chainId);
			listenerService.bootUpTonListeners(true);
		});
	}

	private void initContext(Integer chainId) {
		MDC.clear();

		String requestId = RequestIdGenerator.generate();
		MDC.put(Constants.REQUEST_ID, requestId);
		MDC.put(Constants.CHAIN_ID, String.valueOf(chainId));

		ExecutionContextUtil context = ExecutionContextUtil.getContext();
		context.setRequestId(requestId);
		context.setChainId(chainId);
		context.setMainNet(Objects.equals(chainId, Constants.MAIN_NET_CHAIN_ID));
	}

	private TonNode setupNode(Integer chainId) {
		String baseUrl = secretManagerService.getBaseUrl(String.valueOf(chainId));
		String apiKey = secretManagerService.getApiKey(String.valueOf(chainId));
		String encryptionAlgo = secretManagerService.getEncryptionAlgo(String.valueOf(chainId));
		byte[] encryptionKey = secretManagerService.getEncryptionKey(String.valueOf(chainId));
		long walletId = Long.parseLong(secretManagerService.getWalletId(String.valueOf(chainId)));
		String tonCenterUrl = secretManagerService.getTonCenterUrl(String.valueOf(chainId));
		String tonCenterApiKey = secretManagerService.getTonCenterApiKey(String.valueOf(chainId));
		boolean isMainNet = Constants.MAIN_NET_CHAIN_ID.equals(chainId);
		return TonNode.builder()
				.isMainNet(isMainNet)
				.chainId(chainId)
				.encryptionAlgo(encryptionAlgo)
				.encryptionKey(encryptionKey)
				.tonCenterUrl(tonCenterUrl)
				.tonCenterApiKey(tonCenterApiKey)
				.baseUrl(baseUrl)
				.apiKey(apiKey)
				.listenerBaseUrl(baseUrl)
				.listenerApiKey(apiKey)
				.walletId(walletId)
				.build();
	}
}

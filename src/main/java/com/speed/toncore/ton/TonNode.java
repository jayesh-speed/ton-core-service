package com.speed.toncore.ton;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TonNode {

	private Integer chainId;
	private String baseUrl;
	private String apiKey;
	private boolean isMainNet;
	private String encryptionAlgo;
	private byte[] encryptionKey;
	private String listenerBaseUrl;
	private String listenerApiKey;
	private String tonCenterUrl;
	private String tonCenterApiKey;
	private long walletId;
}

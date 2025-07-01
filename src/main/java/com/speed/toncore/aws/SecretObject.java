package com.speed.toncore.aws;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SecretObject {

	private byte[] encryptionKey;
	private String baseUrl;
	private String apiKey;
	private String listenerBaseUrl;
	private String listenerApiKey;
	private String tonCenterUrl;
	private String tonCenterApiKey;
	private String encryptionAlgo;
	private String chainId;
}

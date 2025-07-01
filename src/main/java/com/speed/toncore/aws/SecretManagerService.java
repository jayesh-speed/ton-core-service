package com.speed.toncore.aws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.speed.javacommon.exceptions.InternalServerErrorException;
import com.speed.toncore.config.SecretManagerConfig;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.InvalidParameterException;
import software.amazon.awssdk.services.secretsmanager.model.InvalidRequestException;
import software.amazon.awssdk.services.secretsmanager.model.PutSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.PutSecretValueResponse;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class SecretManagerService {

	private final SecretManagerConfig secretsManagerConfig;
	private final SecretsManagerClient secretsManagerClient;
	private final SecretObject testNetSecretObject;
	private final SecretObject mainNetSecretObject;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	public SecretManagerService(SecretManagerConfig secretManagerConfig) {
		this.secretsManagerConfig = secretManagerConfig;
		this.secretsManagerClient = SecretsManagerClient.builder().region(Region.of(secretManagerConfig.getRegion())).build();
		this.testNetSecretObject = generateSecretObject(String.valueOf(Constants.TEST_NET_CHAIN_ID));
		this.mainNetSecretObject = generateSecretObject(String.valueOf(Constants.MAIN_NET_CHAIN_ID));
	}

	private SecretObject generateSecretObject(String tonChainId) {
		JsonNode secrets = fetchSecretValues(tonChainId);
		String baseUrl = getSecretValueString(Constants.SecretManagerKeys.BASE_URL, secrets);
		String apiKey = getSecretValueString(Constants.SecretManagerKeys.API_KEY, secrets);
		String encryptionKeyStr = getSecretValueString(Constants.SecretManagerKeys.ENCRYPTION_KEY, secrets);
		byte[] encryptionKey = Base64.getDecoder().decode(encryptionKeyStr);
		String encryptionAlgo = getSecretValueString(Constants.SecretManagerKeys.ENCRYPTION_ALGO, secrets);
		String listenerBaseUrl = getSecretValueString(Constants.SecretManagerKeys.LISTENER_BASE_URL, secrets);
		String listenerApiKey = getSecretValueString(Constants.SecretManagerKeys.LISTENER_API_KEY, secrets);
		String walletId = getSecretValueString(Constants.SecretManagerKeys.WALLET_ID, secrets);
		String tonCenterUrl = getSecretValueString(Constants.SecretManagerKeys.TON_CENTER_URL, secrets);
		String tonCenterApiKey = getSecretValueString(Constants.SecretManagerKeys.TON_CENTER_API_KEY, secrets);
		return SecretObject.builder()
				.baseUrl(baseUrl)
				.apiKey(apiKey)
				.encryptionAlgo(encryptionAlgo)
				.encryptionKey(encryptionKey)
				.listenerBaseUrl(listenerBaseUrl)
				.listenerApiKey(listenerApiKey)
				.chainId(tonChainId)
				.tonCenterUrl(tonCenterUrl)
				.tonCenterApiKey(tonCenterApiKey)
				.walletId(walletId)
				.build();
	}

	public String getBaseUrl(String tonChainId) {
		if (tonChainId.equals(String.valueOf(Constants.MAIN_NET_CHAIN_ID))) {
			return mainNetSecretObject.getBaseUrl();
		}
		return testNetSecretObject.getBaseUrl();
	}

	public String getApiKey(String tonChainId) {
		if (tonChainId.equals(String.valueOf(Constants.MAIN_NET_CHAIN_ID))) {
			return mainNetSecretObject.getApiKey();
		}
		return testNetSecretObject.getApiKey();
	}

	public byte[] getEncryptionKey(String tonChainId) {
		if (tonChainId.equals(String.valueOf(Constants.MAIN_NET_CHAIN_ID))) {
			return mainNetSecretObject.getEncryptionKey();
		}
		return testNetSecretObject.getEncryptionKey();
	}

	public String getEncryptionAlgo(String tonChainId) {
		if (tonChainId.equals(String.valueOf(Constants.MAIN_NET_CHAIN_ID))) {
			return mainNetSecretObject.getEncryptionAlgo();
		}
		return testNetSecretObject.getEncryptionAlgo();
	}

	public String getListenerBaseUrl(String tonChainId) {
		if (tonChainId.equals(String.valueOf(Constants.MAIN_NET_CHAIN_ID))) {
			return mainNetSecretObject.getListenerBaseUrl();
		}
		return testNetSecretObject.getListenerBaseUrl();
	}

	public String getListenerApiKey(String tonChainId) {
		if (tonChainId.equals(String.valueOf(Constants.MAIN_NET_CHAIN_ID))) {
			return mainNetSecretObject.getListenerApiKey();
		}
		return testNetSecretObject.getListenerApiKey();
	}

	public String getTonCenterUrl(String tonChainId) {
		if (tonChainId.equals(String.valueOf(Constants.MAIN_NET_CHAIN_ID))) {
			return mainNetSecretObject.getTonCenterUrl();
		}
		return testNetSecretObject.getTonCenterUrl();
	}

	public String getTonCenterApiKey(String tonChainId) {
		if (tonChainId.equals(String.valueOf(Constants.MAIN_NET_CHAIN_ID))) {
			return mainNetSecretObject.getTonCenterApiKey();
		}
		return testNetSecretObject.getTonCenterApiKey();
	}

	public String getWalletId(String tonChainId) {
		if (tonChainId.equals(String.valueOf(Constants.MAIN_NET_CHAIN_ID))) {
			return mainNetSecretObject.getWalletId();
		}
		return testNetSecretObject.getWalletId();
	}

	private String getSecretValueString(String secretKey, JsonNode secrets) {
		JsonNode jsonNode = secrets.get(secretKey);
		return Objects.nonNull(jsonNode) ? jsonNode.asText() : null;
	}

	private JsonNode fetchSecretValues(String chainId) {
		String secretResult;
		String secretId;
		if (chainId.equals(String.valueOf(Constants.MAIN_NET_CHAIN_ID))) {
			secretId = secretsManagerConfig.getMainnetSecretId();
		} else {
			secretId = secretsManagerConfig.getTestnetSecretId();
		}
		try {
			GetSecretValueRequest valueRequest = GetSecretValueRequest.builder().secretId(secretId).build();
			GetSecretValueResponse secretValueResult = secretsManagerClient.getSecretValue(valueRequest);
			secretResult = secretValueResult.secretString();
			if (Objects.isNull(secretResult)) {
				throw new InternalServerErrorException(String.format(Errors.SECRET_VALUE_NOT_FOUND, secretId));
			}
			return objectMapper.readTree(secretResult);
		} catch (InvalidRequestException e) {
			LOG.error(String.format(Errors.INVALID_REQUEST, e.getMessage()));
		} catch (InvalidParameterException e) {
			LOG.error(String.format(Errors.INVALID_PARAMS, e.getMessage()));
		} catch (IOException e) {
			LOG.error(String.format(Errors.CONNECTION_ERROR_WITH_SECRET_MANAGER, e.getMessage()));
		}
		return objectMapper.createObjectNode();
	}

	public void updateSecret() throws NoSuchAlgorithmException {
		String encryptionAlgo = getEncryptionAlgo(String.valueOf(ExecutionContextUtil.getContext().getChainId()));
		String baseUrl = getBaseUrl(String.valueOf(ExecutionContextUtil.getContext().getChainId()));
		String apiKey = getApiKey(String.valueOf(ExecutionContextUtil.getContext().getChainId()));
		String listenerBaseUrl = getListenerBaseUrl(String.valueOf(ExecutionContextUtil.getContext().getChainId()));
		String listenerApiKey = getListenerApiKey(String.valueOf(ExecutionContextUtil.getContext().getChainId()));
		String walletId = getWalletId(String.valueOf(ExecutionContextUtil.getContext().getChainId()));
		String tonCenterUrl = getTonCenterUrl(String.valueOf(ExecutionContextUtil.getContext().getChainId()));
		String tonCenterApi = getTonCenterApiKey(String.valueOf(ExecutionContextUtil.getContext().getChainId()));
		KeyGenerator keyGenerator = KeyGenerator.getInstance(encryptionAlgo);
		keyGenerator.init(128);
		SecretKey secretKey = keyGenerator.generateKey();
		byte[] generatedSecret = secretKey.getEncoded();
		String encryptionKey = Base64.getEncoder().encodeToString(generatedSecret);
		PutSecretValueRequest.Builder psvr = PutSecretValueRequest.builder();
		Map<String, JsonNode> valueMap = HashMap.newHashMap(5);
		valueMap.put(Constants.SecretManagerKeys.BASE_URL, new TextNode(baseUrl));
		valueMap.put(Constants.SecretManagerKeys.API_KEY, new TextNode(apiKey));
		valueMap.put(Constants.SecretManagerKeys.LISTENER_BASE_URL, new TextNode(listenerBaseUrl));
		valueMap.put(Constants.SecretManagerKeys.LISTENER_API_KEY, new TextNode(listenerApiKey));
		valueMap.put(Constants.SecretManagerKeys.ENCRYPTION_ALGO, new TextNode(encryptionAlgo));
		valueMap.put(Constants.SecretManagerKeys.ENCRYPTION_KEY, new TextNode(encryptionKey));
		valueMap.put(Constants.SecretManagerKeys.WALLET_ID, new TextNode(walletId));
		valueMap.put(Constants.SecretManagerKeys.TON_CENTER_URL, new TextNode(tonCenterUrl));
		valueMap.put(Constants.SecretManagerKeys.TON_CENTER_API_KEY, new TextNode(tonCenterApi));
		ObjectNode jsonNode = new ObjectNode(JsonNodeFactory.instance);
		jsonNode.setAll(valueMap);
		String secretId;
		if (ExecutionContextUtil.getContext().getChainId().equals(Constants.MAIN_NET_CHAIN_ID)) {
			secretId = secretsManagerConfig.getMainnetSecretId();
		} else {
			secretId = secretsManagerConfig.getTestnetSecretId();
		}
		psvr.secretId(secretId);
		psvr.secretString(jsonNode.toString());
		PutSecretValueResponse result = secretsManagerClient.putSecretValue(psvr.build());
	}
}

package com.speed.toncore.aws;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.InvalidParameterException;
import com.amazonaws.services.secretsmanager.model.InvalidRequestException;
import com.amazonaws.services.secretsmanager.model.PutSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.PutSecretValueResult;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.speed.toncore.config.SecretManagerConfig;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
	private final AWSSecretsManager secretsManager;
	private final SecretObject testNetSecretObject;
	private final SecretObject mainNetSecretObject;

	@Autowired
	public SecretManagerService(SecretManagerConfig secretManagerConfig) {
		this.secretsManagerConfig = secretManagerConfig;
		this.secretsManager = AWSSecretsManagerClientBuilder.standard().withRegion(secretManagerConfig.getRegion()).build();
		this.testNetSecretObject = generateSecretObject(String.valueOf(Constants.TEST_NET_CHAIN_ID));
		this.mainNetSecretObject = generateSecretObject(String.valueOf(Constants.MAIN_NET_CHAIN_ID));
	}

	private SecretObject generateSecretObject(String tonChainId) {
		String baseUrl = getSecretValueString(Constants.SecretManagerKeys.BASE_URL, tonChainId);
		String apiKey = getSecretValueString(Constants.SecretManagerKeys.API_KEY, tonChainId);
		String encryptionKeyStr = getSecretValueString(Constants.SecretManagerKeys.ENCRYPTION_KEY, tonChainId);
		byte[] encryptionKey = Base64.getDecoder().decode(encryptionKeyStr);
		String encryptionAlgo = getSecretValueString(Constants.SecretManagerKeys.ENCRYPTION_ALGO, tonChainId);
		String listenerBaseUrl = getSecretValueString(Constants.SecretManagerKeys.LISTENER_BASE_URL, tonChainId);
		String listenerApiKey = getSecretValueString(Constants.SecretManagerKeys.LISTENER_API_KEY, tonChainId);
		String tonCenterUrl = getSecretValueString(Constants.SecretManagerKeys.TON_CENTER_URL, tonChainId);
		String tonCenterApiKey = getSecretValueString(Constants.SecretManagerKeys.TON_CENTER_API_KEY, tonChainId);
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

	private byte[] getSecretValueByte(String secretKey, String tonChainId) {
		try {
			JsonNode jsonNode = getSecretValue(secretKey, tonChainId);
			return Objects.nonNull(jsonNode) ? jsonNode.binaryValue() : new byte[0];
		} catch (IOException e) {
			LOG.error(String.format(Errors.CONNECTION_ERROR_WITH_SECRET_MANAGER, e.getMessage()));
		}
		return new byte[0];
	}

	private String getSecretValueString(String secretKey, String tonChainId) {
		JsonNode jsonNode = getSecretValue(secretKey, tonChainId);
		return Objects.nonNull(jsonNode) ? jsonNode.asText() : null;
	}

	private JsonNode getSecretValue(String secretKey, String tonChainId) {
		String secretResult;
		String secretId;
		if (tonChainId.equals(String.valueOf(Constants.MAIN_NET_CHAIN_ID))) {
			secretId = secretsManagerConfig.getMainnetSecretId();
		} else {
			secretId = secretsManagerConfig.getTestnetSecretId();
		}
		try {
			GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest();
			getSecretValueRequest.setSecretId(secretId);
			GetSecretValueResult secretValueResult = secretsManager.getSecretValue(getSecretValueRequest);
			secretResult = secretValueResult.getSecretString();
			if (Objects.isNull(secretResult)) {
				LOG.error(String.format(Errors.SECRET_VALUE_NOT_FOUND, secretId));
				throw new ResourceNotFoundException(String.format(Errors.SECRET_VALUE_NOT_FOUND, secretId));
			}
			JsonNode jsonNode = new ObjectMapper().readTree(secretResult);
			return jsonNode.get(secretKey);
		} catch (ResourceNotFoundException e) {
			LOG.error(String.format(Errors.SECRET_NOT_FOUND, secretId));
		} catch (InvalidRequestException e) {
			LOG.error(String.format(Errors.INVALID_REQUEST, e.getMessage()));
		} catch (InvalidParameterException e) {
			LOG.error(String.format(Errors.INVALID_PARAMS, e.getMessage()));
		} catch (IOException e) {
			LOG.error(String.format(Errors.CONNECTION_ERROR_WITH_SECRET_MANAGER, e.getMessage()));
		}
		return new ObjectMapper().createObjectNode();
	}

	public void updateSecret() throws NoSuchAlgorithmException {
		String encryptionAlgo = getEncryptionAlgo(String.valueOf(ExecutionContextUtil.getContext().getChainId()));
		String baseUrl = getBaseUrl(String.valueOf(ExecutionContextUtil.getContext().getChainId()));
		String apiKey = getApiKey(String.valueOf(ExecutionContextUtil.getContext().getChainId()));
		String listenerBaseUrl = getListenerBaseUrl(String.valueOf(ExecutionContextUtil.getContext().getChainId()));
		String listenerApiKey = getListenerApiKey(String.valueOf(ExecutionContextUtil.getContext().getChainId()));

		KeyGenerator keyGenerator = KeyGenerator.getInstance(encryptionAlgo);
		keyGenerator.init(128);
		SecretKey secretKey = keyGenerator.generateKey();
		byte[] generatedSecret = secretKey.getEncoded();
		String encryptionKey = Base64.getEncoder().encodeToString(generatedSecret);
		PutSecretValueRequest psvr = new PutSecretValueRequest();
		Map<String, JsonNode> valueMap = HashMap.newHashMap(5);
		valueMap.put(Constants.SecretManagerKeys.BASE_URL, new TextNode(baseUrl));
		valueMap.put(Constants.SecretManagerKeys.API_KEY, new TextNode(apiKey));
		valueMap.put(Constants.SecretManagerKeys.LISTENER_BASE_URL, new TextNode(listenerBaseUrl));
		valueMap.put(Constants.SecretManagerKeys.LISTENER_API_KEY, new TextNode(listenerApiKey));
		valueMap.put(Constants.SecretManagerKeys.ENCRYPTION_ALGO, new TextNode(encryptionAlgo));
		valueMap.put(Constants.SecretManagerKeys.ENCRYPTION_KEY, new TextNode(encryptionKey));
		ObjectNode jsonNode = new ObjectNode(JsonNodeFactory.instance);
		jsonNode.setAll(valueMap);
		String secretId;
		if (ExecutionContextUtil.getContext().getChainId().equals(Constants.MAIN_NET_CHAIN_ID)) {
			secretId = secretsManagerConfig.getMainnetSecretId();
		} else {
			secretId = secretsManagerConfig.getTestnetSecretId();
		}
		psvr.setSecretId(secretId);
		psvr.setSecretString(jsonNode.toString());
		PutSecretValueResult result = secretsManager.putSecretValue(psvr);
	}
}

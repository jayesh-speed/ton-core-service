package com.speed.toncore.ton;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.speed.javacommon.constants.CommonErrors;
import com.speed.javacommon.exceptions.InternalServerErrorException;
import com.speed.javacommon.util.Assert;
import com.speed.javacommon.util.StringUtil;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Endpoints;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.constants.JsonKeys;
import com.speed.toncore.interceptor.RestClientManager;
import com.speed.toncore.pojo.AccountBalanceDto;
import com.speed.toncore.pojo.AccountStatusDto;
import com.speed.toncore.pojo.FeeDto;
import com.speed.toncore.pojo.JettonWalletDto;
import com.speed.toncore.pojo.SendTransactionDto;
import com.speed.toncore.pojo.TonConfigParamDto;
import com.speed.toncore.pojo.TraceDto;
import com.speed.toncore.pojo.WalletInformationDto;
import com.speed.toncore.util.LogMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.retry.RetryException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class TonCoreServiceHelper {

	private static final String URL_TEMPLATE = "%s%s";
	private final RestClientManager restClient;
	private final TonNodePool tonNodePool;
	private final ObjectMapper objectMapper;

	private HttpHeaders getHeaders(String apiKey) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
		return headers;
	}

	@Retryable(retryFor = RetryException.class, backoff = @Backoff(delay = 2000, multiplier = 2, random = true, maxDelay = 8000), maxAttempts = 5)
	public BigInteger getTonBalance(String address) {
		TonNode tonNode = tonNodePool.getTonNodeByChainId();
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(JsonKeys.QueryParameters.ADDRESS, address);
		Map<String, Object> response = restClient.executeAPICall(HttpMethod.GET,
				String.format(URL_TEMPLATE, tonNode.getBaseUrl(), Endpoints.TonIndexer.GET_TON_BALANCE), params, getHeaders(tonNode.getApiKey()));
		AccountBalanceDto accountBalanceDto = parseResponse(response, AccountBalanceDto.class);
		return accountBalanceDto.getResult();
	}

	@Retryable(retryFor = RetryException.class, backoff = @Backoff(delay = 2000, multiplier = 2, random = true, maxDelay = 8000), maxAttempts = 5)
	public String sendMessageWithReturnHash(String messageBoc) {
		TonNode tonNode = tonNodePool.getTonNodeByChainId();
		String url = String.format(URL_TEMPLATE, tonNode.getBaseUrl(), Endpoints.TonIndexer.SEND_MESSAGE_WITH_RETURN_DATA);
		Map<String, Object> requestBody = Map.of(JsonKeys.QueryParameters.BOC, messageBoc);
		Map<String, Object> response = restClient.executeAPICall(HttpMethod.POST, url, null, getHeaders(tonNode.getApiKey()), requestBody);
		SendTransactionDto sendTransactionDto = parseResponse(response, SendTransactionDto.class);
		Assert.nonNull(sendTransactionDto.getMessageHash(), () -> new RetryException(String.format(CommonErrors.ERROR_WHILE_CALLING_URL, url)));
		return sendTransactionDto.getMessageHash();
	}

	@Retryable(retryFor = RetryException.class, backoff = @Backoff(delay = 2000, multiplier = 2, random = true, maxDelay = 8000), maxAttempts = 5)
	public int getSeqNo(String address) {
		WalletInformationDto walletInformation = getWalletInformation(address);
		Assert.nonNull(walletInformation, () -> new RetryException(String.format(Errors.TonIndexer.FAILED_TO_GET_SEQ_NO, address)));
		return walletInformation.getSeqNo();
	}

	@Retryable(retryFor = RetryException.class, backoff = @Backoff(delay = 2000, multiplier = 2, random = true, maxDelay = 8000), maxAttempts = 5)
	public Boolean isDeployed(String address) {
		TonNode tonNode = tonNodePool.getTonNodeByChainId();
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(JsonKeys.QueryParameters.ADDRESS, address);
		Map<String, Object> response = restClient.executeAPICall(HttpMethod.GET,
				String.format(URL_TEMPLATE, tonNode.getBaseUrl(), Endpoints.TonIndexer.GET_ACCOUNT_STATE), params, getHeaders(tonNode.getApiKey()));
		AccountStatusDto accountStatusDto = parseResponse(response, AccountStatusDto.class);
		return accountStatusDto.getResult().equalsIgnoreCase(Constants.ACTIVE);
	}

	@Retryable(retryFor = RetryException.class, backoff = @Backoff(delay = 2000, multiplier = 2, random = true, maxDelay = 8000), maxAttempts = 5)
	public WalletInformationDto getWalletInformation(String address) {
		TonNode tonNode = tonNodePool.getTonNodeByChainId();
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(JsonKeys.QueryParameters.ADDRESS, address);
		params.add(JsonKeys.QueryParameters.USE_V2, Boolean.FALSE.toString());
		Map<String, Object> response = restClient.executeAPICall(HttpMethod.GET,
				String.format(URL_TEMPLATE, tonNode.getBaseUrl(), Endpoints.TonIndexer.GET_WALLET_INFORMATION), params, getHeaders(tonNode.getApiKey()));
		return parseResponse(response, WalletInformationDto.class);
	}

	@Retryable(retryFor = RetryException.class, backoff = @Backoff(delay = 2000, multiplier = 2, random = true, maxDelay = 8000), maxAttempts = 5)
	public JettonWalletDto getJettonWallet(String ownerAddress, String jettonMasterAddress) {
		TonNode tonNode = tonNodePool.getTonNodeByChainId();
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(JsonKeys.QueryParameters.OWNER_ADDRESS, ownerAddress);
		params.add(JsonKeys.QueryParameters.JETTON_MASTER_ADDRESS, jettonMasterAddress);
		params.add(JsonKeys.QueryParameters.EXCLUDE_ZERO_BALANCE, Boolean.FALSE.toString());
		Map<String, Object> response = restClient.executeAPICall(HttpMethod.GET,
				String.format(URL_TEMPLATE, tonNode.getBaseUrl(), Endpoints.TonIndexer.GET_JETTON_WALLET), params, getHeaders(tonNode.getApiKey()));
		return parseResponse(response, JettonWalletDto.class);
	}

	@Retryable(retryFor = RetryException.class, backoff = @Backoff(delay = 2000, multiplier = 2, random = true, maxDelay = 8000), maxAttempts = 5)
	public FeeDto getEstimateFees(String sourceAddress, String requestBody, String initCode, String initData, boolean ignoreChksig) {
		TonNode tonNode = tonNodePool.getTonNodeByChainId();
		Map<String, Object> payload = Map.of(JsonKeys.QueryParameters.ADDRESS, sourceAddress, JsonKeys.QueryParameters.BODY, requestBody,
				JsonKeys.QueryParameters.INIT_CODE, initCode, JsonKeys.QueryParameters.INIT_DATA, initData, JsonKeys.QueryParameters.IGNORE_CHKSIG,
				ignoreChksig);
		Map<String, Object> response = restClient.executeAPICall(HttpMethod.POST,
				String.format(URL_TEMPLATE, tonNode.getBaseUrl(), Endpoints.TonIndexer.GET_ESTIMATE_FEES), null, getHeaders(tonNode.getApiKey()),
				payload);
		return parseResponse(response, FeeDto.class);
	}

	@Retryable(retryFor = RetryException.class, backoff = @Backoff(delay = 2000, multiplier = 2, random = true, maxDelay = 8000), maxAttempts = 5)
	public TraceDto getTraceByTraceId(String traceId) {
		TonNode tonNode = tonNodePool.getTonNodeByChainId();
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		String encodedTraceId = URLEncoder.encode(traceId, StandardCharsets.UTF_8);
		params.add(JsonKeys.QueryParameters.TRACE_ID, encodedTraceId);
		params.add(JsonKeys.QueryParameters.INCLUDE_ACTIONS, Boolean.FALSE.toString());
		params.add(JsonKeys.QueryParameters.SORT, JsonKeys.QueryParameters.SORT_ASC);
		Map<String, Object> response = restClient.executeAPICall(HttpMethod.GET,
				String.format(URL_TEMPLATE, tonNode.getTonCenterUrl(), Endpoints.TonIndexer.GET_TRACE_BY_TRACE_ID), params,
				getHeaders(tonNode.getTonCenterApiKey()));
		TraceDto traceDto = parseResponse(response, TraceDto.class);
		if (traceDto != null && traceDto.getTraces() != null && !traceDto.getTraces().isEmpty()) {
			return traceDto;
		}
		throw new RetryException(String.format(LogMessages.Warn.WAITING_FOR_TRACE_UPDATE, traceId));
	}

	@Retryable(retryFor = RetryException.class, backoff = @Backoff(delay = 2000, multiplier = 2, random = true, maxDelay = 8000), maxAttempts = 5)
	public TonConfigParamDto getConfigParam(Integer configParam) {
		TonNode tonNode = tonNodePool.getTonNodeByChainId();
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(JsonKeys.QueryParameters.CONFIG_ID, configParam.toString());
		Map<String, Object> response = restClient.executeAPICall(HttpMethod.GET,
				String.format(URL_TEMPLATE, tonNode.getBaseUrl(), Endpoints.TonIndexer.GET_CONFIG_PARAM), params, getHeaders(tonNode.getApiKey()), null);
		return parseResponse(response, TonConfigParamDto.class);
	}

	private <T> T parseResponse(Map<String, Object> response, Class<T> clazz) {
		HttpStatus status = (HttpStatus) response.get(RestClientManager.RESPONSE_STATUS);
		if (Objects.isNull(status)) {
			throw new InternalServerErrorException(String.format(Errors.TonIndexer.NULL_STATUS_CODE));
		}
		String responseBody = (String) response.get(RestClientManager.RESPONSE_BODY);
		if (status.is2xxSuccessful()) {
			if (StringUtil.nullOrEmpty(responseBody)) {
				throw new InternalServerErrorException(Errors.TonIndexer.EMPTY_RESPONSE_BODY);
			}
			try {
				return objectMapper.readValue(responseBody, clazz);
			} catch (JsonProcessingException e) {
				throw new InternalServerErrorException(String.format(Errors.TonIndexer.FAILED_TO_PARSE_RESPONSE, responseBody), e);
			}
		}
		if (status.is4xxClientError()) {
			if (status == HttpStatus.TOO_MANY_REQUESTS) {
				throw new RetryException(Errors.TonIndexer.TOO_MANY_REQUEST_ERROR);
			}
			if (StringUtil.nullOrEmpty(responseBody)) {
				throw new InternalServerErrorException(Errors.TonIndexer.EMPTY_RESPONSE_BODY);
			}
		}
		if (status.is5xxServerError()) {
			throw new InternalServerErrorException(String.format(Errors.TonIndexer.TON_INDEXER_INTERNAL_SERVER_ERROR, status.value(), responseBody));
		}
		throw new InternalServerErrorException(String.format(Errors.TonIndexer.UNEXPECTED_ERROR, status.value(), responseBody));
	}
}

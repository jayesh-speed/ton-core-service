package com.speed.toncore.interceptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.speed.javacommon.constants.CommonLogKeys;
import com.speed.javacommon.enums.CommonLogActions;
import com.speed.javacommon.log.LogHolder;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Errors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.marker.Markers;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestClientManager {

	public static final String RESPONSE_BODY = "response_body";
	public static final String RESPONSE_HEADERS = "response_headers";
	public static final String RESPONSE_STATUS = "response_status";

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final List<String> SECURE_HEADER_NAMES = List.of(HttpHeaders.AUTHORIZATION, Constants.X_API_KEY);
	private final RestClient restClient;

	private static void setHeadersToLog(HttpHeaders headers, APIRequest apiRequest) {
		if (Objects.nonNull(headers)) {
			MultiValueMap<String, String> secureHeaders = new LinkedMultiValueMap<>(SECURE_HEADER_NAMES.size());
			SECURE_HEADER_NAMES.forEach(headerName -> {
				if (headers.containsKey(headerName)) {
					List<String> headerValues = headers.remove(headerName);
					secureHeaders.addAll(headerName, headerValues);
				}
			});
			apiRequest.setHeaders(headers.toSingleValueMap());
			headers.addAll(secureHeaders);
		} else {
			apiRequest.setHeaders(null);
		}
	}

	public Map<String, Object> executeAPICall(HttpMethod httpMethod, String requestUrl, HttpHeaders headers) {
		return executeAPICall(httpMethod, requestUrl, null, headers, null);
	}

	public Map<String, Object> executeAPICall(HttpMethod httpMethod, String requestUrl, HttpHeaders headers, Object requestBody) {
		return executeAPICall(httpMethod, requestUrl, null, headers, requestBody);
	}

	public Map<String, Object> executeAPICall(HttpMethod httpMethod, String requestUrl, MultiValueMap<String, String> params, HttpHeaders headers) {
		return executeAPICall(httpMethod, requestUrl, params, headers, null);
	}

	public Map<String, Object> executeAPICall(HttpMethod httpMethod, String requestUrl, MultiValueMap<String, String> params, HttpHeaders headers,
			Object requestBody) {

		LogHolder logHolder = new LogHolder();
		logHolder.setAction(CommonLogActions.API_CALL_OUTWARD);

		String requestBodyString = null;
		if (Objects.nonNull(requestBody)) {
			if (requestBody instanceof String) {
				requestBodyString = (String) requestBody;
			} else {
				try {
					requestBodyString = OBJECT_MAPPER.writeValueAsString(requestBody);
				} catch (JsonProcessingException e) {
					LOG.error(e.getMessage(), e);
					return Collections.emptyMap();
				}
			}
		}

		APIRequest apiRequest = new APIRequest();
		apiRequest.setMethod(httpMethod.name());
		apiRequest.setUri(requestUrl);
		apiRequest.setParameters(Objects.nonNull(params) ? params.toSingleValueMap() : null);
		setHeadersToLog(headers, apiRequest);
		apiRequest.setBody(requestBodyString);

		APICall apiCall = new APICall();
		apiCall.setRequest(apiRequest);

		URI uri = UriComponentsBuilder.fromUriString(requestUrl).queryParams(params).build(true).toUri();
		RestClient.RequestBodySpec spec = restClient.method(httpMethod).uri(uri).headers(httpHeaders -> httpHeaders.addAll(headers));
		if (requestBody != null) {
			spec = spec.body(requestBody);
		}
		logHolder.executionStartTime();
		ResponseEntity<String> response = execute(spec, apiCall);
		logHolder.executionEndTime();
		boolean isSuccess = true;
		String responseString;
		HttpHeaders responseHeaders;
		HttpStatus responseHttpStatus;
		if (Objects.nonNull(response)) {
			APIResponse apiResponse = new APIResponse();
			apiResponse.setStatusCode(response.getStatusCode().value());
			apiResponse.setStatus(response.getStatusCode().toString());
			apiCall.setResponse(apiResponse);

			responseString = response.getBody();
			responseHeaders = response.getHeaders();
			responseHttpStatus = HttpStatus.valueOf(response.getStatusCode().value());
			if (!response.getStatusCode().is2xxSuccessful()) {
				apiResponse.setBody(responseString);
				isSuccess = false;
			}
		} else {
			responseString = null;
			responseHeaders = null;
			responseHttpStatus = null;
		}

		logHolder.put(CommonLogKeys.API_CALL, apiCall);
		if (Objects.nonNull(response) && isSuccess) {
			LOG.info(Markers.appendEntries(logHolder.getAttributes()), null);
		} else {
			LOG.error(Markers.appendEntries(logHolder.getAttributes()), null);
		}

		Map<String, Object> responseMap = HashMap.newHashMap(3);
		responseMap.put(RESPONSE_BODY, responseString);
		responseMap.put(RESPONSE_HEADERS, responseHeaders);
		responseMap.put(RESPONSE_STATUS, responseHttpStatus);
		return responseMap;
	}

	private ResponseEntity<String> execute(RestClient.RequestBodySpec request, APICall apiCall) {
		try {
			return request.retrieve().toEntity(String.class);
		} catch (RestClientResponseException restClientResponseException) {
			LOG.error(Errors.REST_CLIENT_API_ERROR, restClientResponseException);
			int rawStatusCode = restClientResponseException.getStatusCode().value();
			String responseBody = restClientResponseException.getResponseBodyAsString();

			APIResponse apiResponse = new APIResponse();
			apiResponse.setStatusCode(rawStatusCode);
			apiResponse.setStatus(restClientResponseException.getStatusText());
			apiResponse.setBody(responseBody);
			apiCall.setResponse(apiResponse);
			return ResponseEntity.status(rawStatusCode).headers(restClientResponseException.getResponseHeaders()).body(responseBody);
		} catch (Exception e) {
			LOG.error(Errors.REST_CLIENT_API_ERROR, e);
		}
		return null;
	}
}

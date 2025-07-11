package com.speed.toncore.interceptor;

import com.google.common.net.HttpHeaders;
import com.speed.javacommon.enums.CommonLogActions;
import com.speed.javacommon.log.LogHolder;
import com.speed.javacommon.util.JSONUtil;
import com.speed.javacommon.util.RequestIdGenerator;
import com.speed.javacommon.util.StringUtil;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Errors;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.marker.Markers;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class ApiInterceptor implements Filter {

	private static void sendError(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, long startTime, HttpStatus httpStatus,
			String errorMessage) throws IOException {
		// read input stream to fill cache of ContentCachingRequestWrapper
		IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);

		ApiError apiError = new ApiError(httpStatus, errorMessage);
		LogHolder logHolder = new LogHolder();
		logHolder.setAction(CommonLogActions.API_EXCEPTION);
		logHolder.put(Constants.API_ERROR, apiError);
		LOG.error(Markers.appendEntries(logHolder.getAttributes()), null);

		response.setStatus(apiError.getHttpStatus().value());
		response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		try (PrintWriter printWriter = response.getWriter()) {
			printWriter.write(JSONUtil.objectToJson(apiError));
		}

		logApiCall(request, response, startTime);
	}

	private static void logApiCall(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, long startTime) throws IOException {
		LogHolder logHolder = new LogHolder();
		logHolder.setAction(CommonLogActions.API_CALL_INWARD);

		APIRequest apiRequest = new APIRequest();
		apiRequest.setMethod(request.getMethod());
		apiRequest.setUri(request.getRequestURL().toString());
		apiRequest.setServerName(request.getServerName());
		apiRequest.setServerPath(request.getServletPath());

		byte[] buf = request.getContentAsByteArray();
		if (buf.length > 0) {
			try {
				apiRequest.setBody(new String(buf, request.getCharacterEncoding()));
			} catch (Exception e) {
				LOG.error("error in reading request body", e);
			}
		}

		Enumeration<String> parameterNames = request.getParameterNames();
		Map<String, String> parameters = HashMap.newHashMap(2);
		while (parameterNames.hasMoreElements()) {
			String parameterName = parameterNames.nextElement();
			String parameterValue = request.getParameter(parameterName);
			parameters.put(parameterName, parameterValue);
		}
		apiRequest.setParameters(parameters);

		Map<String, String> headers = HashMap.newHashMap(4);
		headers.put(HttpHeaders.REFERER, request.getHeader(HttpHeaders.REFERER));
		headers.put(Constants.USER_ID, request.getHeader(Constants.USER_ID));
		headers.put(HttpHeaders.USER_AGENT, request.getHeader(HttpHeaders.USER_AGENT));
		Enumeration<String> forwardedFor = request.getHeaders(HttpHeaders.X_FORWARDED_FOR);

		JSONArray jsonArray = new JSONArray();
		while (forwardedFor.hasMoreElements()) {
			jsonArray.put(forwardedFor.nextElement());
		}
		headers.put(HttpHeaders.X_FORWARDED_FOR, jsonArray.toString());
		apiRequest.setHeaders(headers);

		APIResponse apiResponse = new APIResponse();
		if (Objects.nonNull(response)) {
			apiResponse.setStatusCode(response.getStatus());
			apiResponse.setStatus(HttpStatus.valueOf(response.getStatus()).getReasonPhrase());
			byte[] buffer = response.getContentAsByteArray();
			if (buffer.length > 0) {
				apiResponse.setBody(new String(buffer, response.getCharacterEncoding()));
				response.copyBodyToResponse();
			}
		} else {
			apiResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
			apiResponse.setStatus(HttpStatus.BAD_REQUEST.getReasonPhrase());
			LOG.error("response is null");
		}

		APICall apiCall = new APICall();
		apiCall.setRequest(apiRequest);
		apiCall.setResponse(apiResponse);

		logHolder.put(Constants.API_CALL, apiCall);
		logHolder.put(Constants.ELAPSE_TIME, System.currentTimeMillis() - startTime);
		LOG.info(Markers.appendEntries(logHolder.getAttributes()), null);
	}

	@Override
	public void init(FilterConfig filterConfig) {
		SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, filterConfig.getServletContext());
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

		long startTime = System.currentTimeMillis();

		MDC.clear();

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		String requestId = request.getHeader(Constants.REQUEST_ID);
		if (Objects.isNull(requestId)) {
			requestId = RequestIdGenerator.generate();
		}
		MDC.put(Constants.REQUEST_ID, requestId);

		ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
		ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

		String mode = request.getHeader(Constants.MODE);
		if (wrappedRequest.getRequestURL().toString().endsWith("health")) {
			mode = Constants.FALSE;
		}
		if (StringUtil.nullOrEmpty(mode)) {
			sendError(wrappedRequest, wrappedResponse, startTime, HttpStatus.BAD_REQUEST, String.format(Errors.MISSING_HEADER, Constants.MODE));
			return;
		}
		boolean mainNet = Boolean.parseBoolean(mode);
		MDC.put(Constants.MAIN_NET, String.valueOf(mainNet));

		String accountType = request.getHeader(Constants.ACCOUNT_TYPE);
		accountType = StringUtil.nullOrEmpty(accountType) ? Constants.WALLET : accountType;


		/* For "Access-Control-Allow-Origin" */
		response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
		response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
		response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
				String.join(", ", HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name(), HttpMethod.DELETE.name(),
						HttpMethod.OPTIONS.name(), HttpMethod.PATCH.name()));
		response.setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
		response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
				String.join(",", HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.X_REQUESTED_WITH, "remember-me", HttpHeaders.AUTHORIZATION));
		response.setHeader(Constants.REQUEST_ID, requestId);

		if (HttpMethod.OPTIONS.name().equals(wrappedRequest.getMethod())) {
			response.setStatus(HttpStatus.OK.value());
			chain.doFilter(wrappedRequest, wrappedResponse);
			return;
		}
		accountType = accountType.toUpperCase();
		if (StringUtil.nullOrEmpty(accountType)) {
			sendError(wrappedRequest, wrappedResponse, startTime, HttpStatus.BAD_REQUEST, Errors.ERROR_WITH_ACCOUNT_TYPE);
			return;
		}
		MDC.put(Constants.ACCOUNT_TYPE, accountType);

		init(requestId, mainNet, accountType);

		try {
			chain.doFilter(wrappedRequest, wrappedResponse);
			if (!wrappedRequest.getRequestURL().toString().endsWith("health")) {
				logApiCall(wrappedRequest, wrappedResponse, startTime);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			sendError(wrappedRequest, wrappedResponse, startTime, HttpStatus.INTERNAL_SERVER_ERROR,
					String.format(Errors.INTERNAL_SERVER_ERROR, requestId));
		} finally {
			ExecutionContextUtil.getContext().destroy();
		}
	}

	private void init(String requestId, boolean mainNet, String accountType) {
		ExecutionContextUtil context = ExecutionContextUtil.getContext();
		context.setRequestId(requestId);
		context.setMainNet(mainNet);
		context.setChainId(mainNet ? Constants.MAIN_NET_CHAIN_ID : Constants.TEST_NET_CHAIN_ID);
		context.setAccountTypeName(accountType);
	}
}

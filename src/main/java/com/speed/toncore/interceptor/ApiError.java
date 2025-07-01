package com.speed.toncore.interceptor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.speed.toncore.constants.JsonKeys;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Set;

@Getter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ApiError {

	@JsonIgnore
	private final HttpStatus httpStatus;

	@JsonProperty(JsonKeys.ERRORS)
	private final Set<String> errors;

	@JsonProperty(JsonKeys.TYPE)
	private final String errorType;

	public ApiError(HttpStatus httpStatus, Set<String> errors) {
		this(httpStatus, errors, null);
	}

	public ApiError(HttpStatus httpStatus, String errorMessage) {
		this(httpStatus, errorMessage, null);
	}

	public ApiError(HttpStatus httpStatus, String errorMessage, String errorType) {
		this(httpStatus, Collections.singleton(errorMessage), errorType);
	}
}

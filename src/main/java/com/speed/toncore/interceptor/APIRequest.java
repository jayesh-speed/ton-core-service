package com.speed.toncore.interceptor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.speed.toncore.constants.JsonKeys;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class APIRequest {

	private String method;

	private String uri;

	@JsonProperty(JsonKeys.SERVER_NAME)
	private String serverName;

	@JsonProperty(JsonKeys.SERVER_PATH)
	private String serverPath;

	@JsonProperty(JsonKeys.PARAMETERS)
	private Map<String, String> parameters;

	private Map<String, String> headers;

	private String body;
}

package com.speed.toncore.events;

import com.speed.toncore.interceptor.ExecutionContextUtil;
import lombok.Getter;
import org.slf4j.MDC;

import java.util.Map;

@Getter
public class BaseEvent {

	private final Map<String, String> contextMap = MDC.getCopyOfContextMap();

	private final ExecutionContextUtil executionContextUtil = ExecutionContextUtil.getContext();
}

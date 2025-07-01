package com.speed.toncore.util;

import com.speed.javacommon.enums.CommonLogActions;
import com.speed.javacommon.log.LogHolder;
import com.speed.toncore.constants.LogKeys;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.marker.Markers;
import org.slf4j.MDC;

import java.util.Map;
import java.util.Objects;

@Slf4j
public class AsyncExecutionUtil {

	protected static void initContext(LogHolder logHolder, Map<String, String> contextMap, ExecutionContextUtil context,
			StackTraceElement stackTraceElement) {
		logHolder.setAction(CommonLogActions.ASYNC_EXECUTION);
		if (Objects.nonNull(contextMap)) {
			MDC.setContextMap(contextMap);
		}
		if (Objects.nonNull(context)) {
			ExecutionContextUtil newContext = ExecutionContextUtil.getContext();
			newContext.clone(context);
		}
		if (Objects.nonNull(stackTraceElement)) {
			logHolder.put(LogKeys.CLASS_NAME, stackTraceElement.getClassName());
			logHolder.put(LogKeys.METHOD_NAME, stackTraceElement.getMethodName());
			logHolder.put(LogKeys.FILE_NAME, stackTraceElement.getFileName());
			logHolder.put(LogKeys.LINE_NUMBER, stackTraceElement.getLineNumber());
		}
		logHolder.executionStartTime();
	}

	protected static void logAndDestroyContext(LogHolder logHolder) {
		logHolder.executionEndTime();
		LOG.info(Markers.appendEntries(logHolder.getAttributes()), null);

		MDC.clear();
		ExecutionContextUtil.getContext().destroy();
	}
}

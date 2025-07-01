package com.speed.toncore.config;

import com.speed.toncore.interceptor.ExecutionContextUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;
import java.util.Objects;

@Slf4j
@NoArgsConstructor
public class AsyncContextResolver implements TaskDecorator {

	@Override
	public Runnable decorate(Runnable runnable) {
		Map<String, String> contextMap = MDC.getCopyOfContextMap();
		ExecutionContextUtil context = ExecutionContextUtil.getContext();
		return () -> {
			try {
				if (Objects.nonNull(contextMap)) {
					MDC.setContextMap(contextMap);
				}
				if (Objects.nonNull(context)) {
					ExecutionContextUtil newContext = ExecutionContextUtil.getContext();
					newContext.clone(context);
				}
				runnable.run();
			} finally {
				ExecutionContextUtil.getContext().destroy();
				MDC.clear();
			}
		};
	}
}

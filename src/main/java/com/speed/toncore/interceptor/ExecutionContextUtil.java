package com.speed.toncore.interceptor;

import com.speed.javacommon.util.interceptors.ImprovedThreadLocal;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class ExecutionContextUtil implements Serializable {

	// Atomic integer containing the next thread ID to be assigned
	static final AtomicInteger nextId = new AtomicInteger(0);
	private static final long serialVersionUID = -6751490154133933000L;
	private static final ThreadLocal<ExecutionContextUtil> EXECUTION_CONTEXT = new ImprovedThreadLocal<>() {

		@Override
		protected ExecutionContextUtil initialValue() {
			return new ExecutionContextUtil(nextId.getAndIncrement());
		}
	};
	private final int threadId;
	private String requestId;
	private boolean mainNet;
	private String accountTypeName;
	private Integer chainId;

	ExecutionContextUtil(int threadId) {
		this.threadId = threadId;
	}

	public static synchronized ExecutionContextUtil getContext() {
		if (Objects.isNull(EXECUTION_CONTEXT.get())) {
			EXECUTION_CONTEXT.set(new ExecutionContextUtil(nextId.getAndIncrement()));
		}
		return EXECUTION_CONTEXT.get();
	}

	public void destroy() {
		EXECUTION_CONTEXT.remove();
	}

	public void clone(ExecutionContextUtil context) {
		setRequestId(context.getRequestId());
		setMainNet(context.isMainNet());
		setAccountTypeName(context.getAccountTypeName());
		setChainId(context.getChainId());
	}
}

package com.speed.toncore.util;

import com.speed.javacommon.constants.CommonConstants;
import com.speed.javacommon.constants.CommonLogKeys;
import com.speed.javacommon.exceptions.InternalServerErrorException;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.constants.LogKeys;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.support.Acknowledgment;

import java.util.Objects;

@UtilityClass
@Slf4j
public class ConsumerUtil {

	public void initConsumer(String topicName, int partition, long offset, String consumerGroupId) {
		MDC.put(CommonLogKeys.TOPIC, topicName);
		MDC.put(CommonLogKeys.PARTITION, String.valueOf(partition));
		MDC.put(CommonLogKeys.OFFSET, String.valueOf(offset));
		MDC.put(CommonLogKeys.CONSUMER_GROUP_ID, consumerGroupId);
	}

	public void validateIncomingMessage(Object incomingMessage) {
		if (Objects.isNull(incomingMessage)) {
			LOG.warn(Errors.EMPTY_KAFKA_MESSAGE_RECEIVED);
			throw new InternalServerErrorException(Errors.EMPTY_KAFKA_MESSAGE_RECEIVED);
		}
	}

	public void handleAfterProcesses(Acknowledgment acknowledgment, long startTime, String logs) {
		long endTime = System.currentTimeMillis() - startTime;
		MDC.put(CommonConstants.ELAPSE_TIME, endTime + "");
		LOG.info(logs);
		MDC.clear();
		ExecutionContextUtil.getContext().destroy();
		acknowledgment.acknowledge();
	}

	public void initContext(boolean liveMode, boolean useMainnet) {
		MDC.put(LogKeys.LIVE_MODE, String.valueOf(liveMode));
		boolean mainNet = liveMode && useMainnet;
		MDC.put(Constants.MAIN_NET, String.valueOf(mainNet));

		ExecutionContextUtil.getContext().setMainNet(mainNet);
		if (mainNet) {
			ExecutionContextUtil.getContext().setChainId(Constants.MAIN_NET_CHAIN_ID);
		} else {
			ExecutionContextUtil.getContext().setChainId(Constants.TEST_NET_CHAIN_ID);
		}
	}
}

package com.speed.toncore.service;

import com.speed.toncore.config.AppConfig;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.constants.LogKeys;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import com.speed.toncore.listener.service.TonListenerService;
import com.speed.toncore.util.ConsumerUtil;
import com.speed.toncore.util.LogMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.json.JSONException;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class KafkaConsumer {

	private final AppConfig appConfig;
	private final TonListenerService listenerService;

	@KafkaListener(topics = "${speed.topic.scheduler}", groupId = Constants.ConsumerGroupIds.SCHEDULER_GROUP, containerFactory = "kafkaListenerContainerJson", autoStartup = "${speed.topic.scheduler.startup}")
	public void consumeScheduler(ConsumerRecord<String, String> consumerRecord) {
		try {
			ConsumerUtil.initConsumer(consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset(),
					Constants.ConsumerGroupIds.SCHEDULER_GROUP);
			bootupTestNetListeners();
			bootupMainNetListeners();
		} catch (JSONException e) {
			LOG.error(Errors.ERROR_WHILE_PARSING_MESSAGE, e);
		} finally {
			MDC.clear();
			ExecutionContextUtil.getContext().destroy();
		}
	}

	private void bootupMainNetListeners() {
		initConsumer(true);
		LOG.info(String.format(LogMessages.Info.UPDATING_IDLE_LISTENER, Constants.MAIN_NET_CHAIN_ID));
		listenerService.bootUpTonListeners(false);
	}

	private void bootupTestNetListeners() {
		initConsumer(false);
		LOG.info(String.format(LogMessages.Info.UPDATING_IDLE_LISTENER, Constants.TEST_NET_CHAIN_ID));
		listenerService.bootUpTonListeners(false);
	}

	private void initConsumer(boolean liveMode) {
		MDC.put(LogKeys.LIVE_MODE, String.valueOf(liveMode));

		boolean mainNet = liveMode && appConfig.isUseMainnet();
		MDC.put(Constants.MAIN_NET, String.valueOf(mainNet));
		ExecutionContextUtil.getContext().setMainNet(mainNet);
		if (mainNet) {
			ExecutionContextUtil.getContext().setChainId(Constants.MAIN_NET_CHAIN_ID);
		} else {
			ExecutionContextUtil.getContext().setChainId(Constants.TEST_NET_CHAIN_ID);
		}
	}
}

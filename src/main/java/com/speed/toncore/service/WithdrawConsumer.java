package com.speed.toncore.service;

import com.speed.javacommon.util.DateTimeUtil;
import com.speed.javacommon.util.RequestIdGenerator;
import com.speed.toncore.config.AppConfig;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.constants.LogKeys;
import com.speed.toncore.enums.WithdrawProcessReference;
import com.speed.toncore.enums.WithdrawProcessStatus;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import com.speed.toncore.pojo.WithdrawProcessPojo;
import com.speed.toncore.util.ConsumerUtil;
import com.speed.toncore.util.LogMessages;
import com.speed.toncore.util.OperationPredicateUtil;
import com.speed.toncore.util.TonUtil;
import com.speed.toncore.withdraw.request.WithdrawRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import speed_core_cdc.speed_core_test.tbl_withdraw.Envelope;
import speed_core_cdc.speed_core_test.tbl_withdraw.Value;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class WithdrawConsumer {

	private final AppConfig appConfig;
	private final WithdrawProcessHelper withdrawProcessHelper;

	private static void setWithdrawLoggingProperties(String requestId, String accountId, String speedAccountTypeName, String withdrawId) {
		String request = Objects.nonNull(requestId) ? requestId : RequestIdGenerator.generate();
		ExecutionContextUtil contextUtil = ExecutionContextUtil.getContext();
		contextUtil.setRequestId(request);
		contextUtil.setAccountTypeName(speedAccountTypeName);
		MDC.put(LogKeys.REQUEST_ID, request);
		MDC.put(LogKeys.ACCOUNT_ID, accountId);
		MDC.put(Constants.ACCOUNT_TYPE, speedAccountTypeName);
		MDC.put(LogKeys.WITHDRAW_ID, withdrawId);
	}

	@KafkaListener(topics = "${speed.topic.withdraw-test-mode}", groupId = Constants.ConsumerGroupIds.TON_USDT_WITHDRAW_TEST, containerFactory = "exactlyOnceDeliveryKafkaListener", autoStartup = "${speed.topic.usdt-withdraw-test-mode.startup}")
	public void consumeUSDTWithdrawTestMode(ConsumerRecord<String, Envelope> consumerRecord, Acknowledgment acknowledgment) {
		long startTime = DateTimeUtil.currentEpochMilliSecondsUTC();
		ConsumerUtil.validateIncomingMessage(consumerRecord);
		try {
			ConsumerUtil.initConsumer(consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset(),
					Constants.ConsumerGroupIds.TON_USDT_WITHDRAW_TEST);
			ConsumerUtil.initContext(false, appConfig.isUseMainnet());

			Envelope envelope = consumerRecord.value();
			ConsumerUtil.validateIncomingMessage(envelope);
			Value afterWithdraw = envelope.getAfter();
			String targetCurrency = afterWithdraw.getTargetCurrency();
			if (OperationPredicateUtil.isEntityCreated().test(envelope.getOp()) && Constants.USDT_CURRENCY_SYMBOL.equalsIgnoreCase(targetCurrency) &&
					Constants.PROCESSED_BY_TON.equalsIgnoreCase(afterWithdraw.getProcessedBy()) &&
					WithdrawProcessStatus.UNPAID.name().equalsIgnoreCase(afterWithdraw.getStatus())) {
				LOG.info(String.format(LogMessages.Info.WITHDRAW_TEST_CONSUMER_INVOKED, targetCurrency));

				processTestWithdrawEvent(afterWithdraw, targetCurrency);
			}
		} catch (Exception e) {
			LOG.error(Errors.ERROR_PROCESSING_WITHDRAW, e);
		} finally {
			ConsumerUtil.handleAfterProcesses(acknowledgment, startTime, LogMessages.Info.WITHDRAW_TEST_CONSUMER_COMPLETED);
		}
	}

	@KafkaListener(topics = "${speed.topic.withdraw-live-mode}", groupId = Constants.ConsumerGroupIds.TON_USDT_WITHDRAW_LIVE, containerFactory = "exactlyOnceDeliveryKafkaListener", autoStartup = "${speed.topic.usdt-withdraw-live-mode.startup}")
	public void consumeUSDTWithdrawLiveMode(ConsumerRecord<String, speed_core_cdc.speed_core_live.tbl_withdraw.Envelope> consumerRecord,
			Acknowledgment acknowledgment) {
		long startTime = DateTimeUtil.currentEpochMilliSecondsUTC();
		ConsumerUtil.validateIncomingMessage(consumerRecord);
		try {
			ConsumerUtil.initConsumer(consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset(),
					Constants.ConsumerGroupIds.TON_USDT_WITHDRAW_LIVE);
			ConsumerUtil.initContext(true, appConfig.isUseMainnet());

			speed_core_cdc.speed_core_live.tbl_withdraw.Envelope envelope = consumerRecord.value();
			ConsumerUtil.validateIncomingMessage(envelope);
			speed_core_cdc.speed_core_live.tbl_withdraw.Value afterWithdraw = envelope.getAfter();
			String targetCurrency = afterWithdraw.getTargetCurrency();
			if (OperationPredicateUtil.isEntityCreated().test(envelope.getOp()) && Constants.USDT_CURRENCY_SYMBOL.equalsIgnoreCase(targetCurrency) &&
					Constants.PROCESSED_BY_TON.equalsIgnoreCase(afterWithdraw.getProcessedBy()) &&
					WithdrawProcessStatus.UNPAID.name().equalsIgnoreCase(afterWithdraw.getStatus())) {
				LOG.info(String.format(LogMessages.Info.WITHDRAW_LIVE_CONSUMER_INVOKED, targetCurrency));
				processLiveWithdrawEvent(afterWithdraw, targetCurrency);
			}
		} catch (Exception e) {
			LOG.error(Errors.ERROR_PROCESSING_WITHDRAW, e);
		} finally {
			ConsumerUtil.handleAfterProcesses(acknowledgment, startTime, LogMessages.Info.WITHDRAW_LIVE_CONSUMER_COMPLETED);
		}
	}

	private void processTestWithdrawEvent(Value afterWithdraw, String targetCurrency) {
		String accountId = afterWithdraw.getAccountId();
		String withdrawId = afterWithdraw.getId();
		String speedAccountTypeName = afterWithdraw.getSpeedAccountType().toLowerCase();

		setWithdrawLoggingProperties(afterWithdraw.getSpeedRequest(), accountId, speedAccountTypeName, withdrawId);
		WithdrawProcessPojo withdrawProcessPojo = WithdrawProcessPojo.builder()
				.accountId(afterWithdraw.getAccountId())
				.referenceId(afterWithdraw.getId())
				.referenceType(WithdrawProcessReference.WITHDRAW.name())
				.targetAmount(afterWithdraw.getTargetAmount())
				.targetCurrency(targetCurrency)
				.withdrawRequest(afterWithdraw.getWithdrawRequest())
				.withdrawType(afterWithdraw.getWithdrawMethod())
				.build();
		withdrawProcessHelper.saveWithdrawProcess(withdrawProcessPojo);

		if (isInvalidWithdraw(withdrawProcessPojo)) {
			return;
		}

		WithdrawRequest withdrawRequest = getWithdrawRequest(afterWithdraw, targetCurrency);
		withdrawProcessHelper.processWithdraw(withdrawRequest, afterWithdraw.getAccountId(), afterWithdraw.getId());
	}

	private void processLiveWithdrawEvent(speed_core_cdc.speed_core_live.tbl_withdraw.Value afterWithdraw, String targetCurrency) {
		String accountId = afterWithdraw.getAccountId();
		String withdrawId = afterWithdraw.getId();
		String speedAccountTypeName = afterWithdraw.getSpeedAccountType().toLowerCase();

		setWithdrawLoggingProperties(afterWithdraw.getSpeedRequest(), accountId, speedAccountTypeName, withdrawId);
		WithdrawProcessPojo withdrawProcessPojo = WithdrawProcessPojo.builder()
				.accountId(afterWithdraw.getAccountId())
				.referenceId(afterWithdraw.getId())
				.referenceType(WithdrawProcessReference.WITHDRAW.name())
				.targetAmount(afterWithdraw.getTargetAmount())
				.targetCurrency(targetCurrency)
				.withdrawRequest(afterWithdraw.getWithdrawRequest())
				.withdrawType(afterWithdraw.getWithdrawMethod())
				.build();
		withdrawProcessHelper.saveWithdrawProcess(withdrawProcessPojo);

		if (isInvalidWithdraw(withdrawProcessPojo)) {
			return;
		}

		WithdrawRequest withdrawRequest = getWithdrawRequest(afterWithdraw, targetCurrency);
		withdrawProcessHelper.processWithdraw(withdrawRequest, afterWithdraw.getAccountId(), afterWithdraw.getId());
	}

	private boolean isInvalidWithdraw(WithdrawProcessPojo withdrawProcessPojo) {
		if (!Constants.TON_WITHDRAW_METHOD.equalsIgnoreCase(withdrawProcessPojo.getWithdrawType())) {
			withdrawProcessHelper.markWithdrawProcessFailed(Errors.ERROR_WITHDRAW_METHOD, withdrawProcessPojo.getAccountId(),
					withdrawProcessPojo.getReferenceId());
			return true;
		}
		if (Objects.isNull(withdrawProcessPojo.getWithdrawRequest())) {
			withdrawProcessHelper.markWithdrawProcessFailed(Errors.ERROR_WITHDRAW_REQUEST, withdrawProcessPojo.getAccountId(),
					withdrawProcessPojo.getReferenceId());
			return true;
		}
		if (Objects.isNull(withdrawProcessPojo.getTargetAmount()) || BigDecimal.ZERO.equals(withdrawProcessPojo.getTargetAmount())) {
			withdrawProcessHelper.markWithdrawProcessFailed(Errors.ERROR_WITHDRAW_AMOUNT, withdrawProcessPojo.getAccountId(),
					withdrawProcessPojo.getReferenceId());
			return true;
		}
		return false;
	}

	private WithdrawRequest getWithdrawRequest(Value afterWithdraw, String targetCurrency) {
		WithdrawRequest withdrawRequest = new WithdrawRequest();
		withdrawRequest.setToAddress(TonUtil.toRawAddress(afterWithdraw.getWithdrawRequest()));
		withdrawRequest.setValue(String.valueOf(afterWithdraw.getTargetAmount().divide(new BigDecimal(100))));
		withdrawRequest.setTokenSymbol(targetCurrency);
		withdrawRequest.setFromAddress(TonUtil.toRawAddress(afterWithdraw.getPublicKey()));
		return withdrawRequest;
	}

	private WithdrawRequest getWithdrawRequest(speed_core_cdc.speed_core_live.tbl_withdraw.Value afterWithdraw, String targetCurrency) {
		WithdrawRequest withdrawRequest = new WithdrawRequest();
		withdrawRequest.setToAddress(TonUtil.toRawAddress(afterWithdraw.getWithdrawRequest()));
		withdrawRequest.setValue(String.valueOf(afterWithdraw.getTargetAmount().divide(new BigDecimal(100))));
		withdrawRequest.setTokenSymbol(targetCurrency);
		withdrawRequest.setFromAddress(TonUtil.toRawAddress(afterWithdraw.getPublicKey()));
		return withdrawRequest;
	}
}

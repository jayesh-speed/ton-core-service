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
import com.speed.toncore.util.TonUtil;
import com.speed.toncore.withdraw.request.WithdrawRequest;
import com.speed.toncore.pojo.WithdrawProcessPojo;
import com.speed.toncore.util.ConsumerUtil;
import com.speed.toncore.util.LogMessages;
import com.speed.toncore.util.OperationPredicateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import speed_core_cdc.speed_core_test.tbl_payout.Envelope;
import speed_core_cdc.speed_core_test.tbl_payout.Value;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PayoutConsumer {

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

	@KafkaListener(topics = "${speed.topic.payout-test-mode}", groupId = Constants.ConsumerGroupIds.TON_USDT_PAYOUT_TEST, containerFactory = "exactlyOnceDeliveryKafkaListener", autoStartup = "${speed.topic.usdt-payout-test-mode.startup}")
	public void consumeUSDTPayoutTestMode(ConsumerRecord<String, Envelope> consumerRecord, Acknowledgment acknowledgment) {
		long startTime = DateTimeUtil.currentEpochMilliSecondsUTC();
		ConsumerUtil.validateIncomingMessage(consumerRecord);
		try {
			ConsumerUtil.initConsumer(consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset(),
					Constants.ConsumerGroupIds.TON_USDT_PAYOUT_TEST);
			ConsumerUtil.initContext(false, appConfig.isUseMainnet());

			Envelope envelope = consumerRecord.value();
			ConsumerUtil.validateIncomingMessage(envelope);
			Value afterPayout = envelope.getAfter();
			String targetCurrency = afterPayout.getTargetCurrency();
			if (OperationPredicateUtil.isEntityCreated().test(envelope.getOp()) && Constants.USDT_CURRENCY_SYMBOL.equalsIgnoreCase(targetCurrency) &&
					Constants.PROCESSED_BY_TON.equalsIgnoreCase(afterPayout.getProcessedBy()) &&
					WithdrawProcessStatus.UNPAID.name().equalsIgnoreCase(afterPayout.getStatus())) {
				processTestPayoutEvent(afterPayout, targetCurrency);
			}
		} catch (Exception e) {
			LOG.error(Errors.ERROR_PROCESSING_PAYOUT, e);
		} finally {
			ConsumerUtil.handleAfterProcesses(acknowledgment, startTime, LogMessages.Info.PAYOUT_TEST_CONSUMER_COMPLETED);
		}
	}

	@KafkaListener(topics = "${speed.topic.payout-live-mode}", groupId = Constants.ConsumerGroupIds.TON_USDT_PAYOUT_LIVE, containerFactory = "exactlyOnceDeliveryKafkaListener", autoStartup = "${speed.topic.usdt-payout-live-mode.startup}")
	public void consumeUSDTPayoutLiveMode(ConsumerRecord<String, speed_core_cdc.speed_core_live.tbl_payout.Envelope> consumerRecord,
			Acknowledgment acknowledgment) {
		long startTime = DateTimeUtil.currentEpochMilliSecondsUTC();
		ConsumerUtil.validateIncomingMessage(consumerRecord);
		try {
			ConsumerUtil.initConsumer(consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset(),
					Constants.ConsumerGroupIds.TON_USDT_PAYOUT_LIVE);
			ConsumerUtil.initContext(true, appConfig.isUseMainnet());

			speed_core_cdc.speed_core_live.tbl_payout.Envelope envelope = consumerRecord.value();
			ConsumerUtil.validateIncomingMessage(envelope);
			speed_core_cdc.speed_core_live.tbl_payout.Value afterPayout = envelope.getAfter();
			String targetCurrency = afterPayout.getTargetCurrency();
			if (OperationPredicateUtil.isEntityCreated().test(envelope.getOp()) && Constants.USDT_CURRENCY_SYMBOL.equalsIgnoreCase(targetCurrency) &&
					Constants.PROCESSED_BY_TON.equalsIgnoreCase(afterPayout.getProcessedBy()) &&
					WithdrawProcessStatus.UNPAID.name().equalsIgnoreCase(afterPayout.getStatus())) {
				processLivePayoutEvent(afterPayout, targetCurrency);
			}
		} catch (Exception e) {
			LOG.error(Errors.ERROR_PROCESSING_PAYOUT, e);
		} finally {
			ConsumerUtil.handleAfterProcesses(acknowledgment, startTime, LogMessages.Info.PAYOUT_LIVE_CONSUMER_COMPLETED);
		}
	}

	private void processTestPayoutEvent(Value afterPayout, String targetCurrency) {
		LOG.info(String.format(LogMessages.Info.PAYOUT_TEST_CONSUMER_INVOKED, targetCurrency));

		String accountId = afterPayout.getAccountId();
		String withdrawId = afterPayout.getId();
		String speedAccountTypeName = afterPayout.getSpeedAccountType().toLowerCase();

		setWithdrawLoggingProperties(afterPayout.getSpeedRequest(), accountId, speedAccountTypeName, withdrawId);
		WithdrawProcessPojo withdrawProcessPojo = WithdrawProcessPojo.builder()
				.accountId(afterPayout.getAccountId())
				.referenceId(afterPayout.getId())
				.referenceType(WithdrawProcessReference.PAYOUT.name())
				.targetAmount(afterPayout.getTargetAmount())
				.targetCurrency(targetCurrency)
				.withdrawRequest(afterPayout.getPayoutRequest())
				.withdrawType(afterPayout.getPayoutMethod())
				.build();
		withdrawProcessHelper.saveWithdrawProcess(withdrawProcessPojo);

		if (isInvalidPayout(withdrawProcessPojo)) {
			return;
		}

		WithdrawRequest withdrawRequest = getWithdrawRequest(afterPayout, targetCurrency);
		withdrawProcessHelper.processWithdraw(withdrawRequest, afterPayout.getAccountId(), afterPayout.getId());
	}

	private void processLivePayoutEvent(speed_core_cdc.speed_core_live.tbl_payout.Value afterPayout, String targetCurrency) {
		LOG.info(String.format(LogMessages.Info.PAYOUT_LIVE_CONSUMER_INVOKED, targetCurrency));

		String accountId = afterPayout.getAccountId();
		String withdrawId = afterPayout.getId();
		String speedAccountTypeName = afterPayout.getSpeedAccountType().toLowerCase();

		setWithdrawLoggingProperties(afterPayout.getSpeedRequest(), accountId, speedAccountTypeName, withdrawId);
		WithdrawProcessPojo withdrawProcessPojo = WithdrawProcessPojo.builder()
				.accountId(afterPayout.getAccountId())
				.referenceId(afterPayout.getId())
				.referenceType(WithdrawProcessReference.PAYOUT.name())
				.targetAmount(afterPayout.getTargetAmount())
				.targetCurrency(targetCurrency)
				.withdrawRequest(afterPayout.getPayoutRequest())
				.withdrawType(afterPayout.getPayoutMethod())
				.build();
		withdrawProcessHelper.saveWithdrawProcess(withdrawProcessPojo);

		if (isInvalidPayout(withdrawProcessPojo)) {
			return;
		}
		WithdrawRequest withdrawRequest = getWithdrawRequest(afterPayout, targetCurrency);
		withdrawProcessHelper.processWithdraw(withdrawRequest, afterPayout.getAccountId(), afterPayout.getId());
	}

	private boolean isInvalidPayout(WithdrawProcessPojo withdrawProcessPojo) {
		if (!Constants.TON_WITHDRAW_METHOD.equalsIgnoreCase(withdrawProcessPojo.getWithdrawType())) {
			withdrawProcessHelper.markWithdrawProcessFailed(Errors.ERROR_PAYOUT_METHOD, withdrawProcessPojo.getAccountId(),
					withdrawProcessPojo.getReferenceId());
			return true;
		}
		if (Objects.isNull(withdrawProcessPojo.getWithdrawRequest())) {
			withdrawProcessHelper.markWithdrawProcessFailed(Errors.ERROR_PAYOUT_REQUEST, withdrawProcessPojo.getAccountId(),
					withdrawProcessPojo.getReferenceId());
			return true;
		}
		if (Objects.isNull(withdrawProcessPojo.getTargetAmount()) || BigDecimal.ZERO.equals(withdrawProcessPojo.getTargetAmount())) {
			withdrawProcessHelper.markWithdrawProcessFailed(Errors.ERROR_PAYOUT_AMOUNT, withdrawProcessPojo.getAccountId(),
					withdrawProcessPojo.getReferenceId());
			return true;
		}
		return false;
	}

	private WithdrawRequest getWithdrawRequest(Value afterPayout, String targetCurrency) {
		WithdrawRequest withdrawRequest = new WithdrawRequest();
		withdrawRequest.setToAddress(TonUtil.toRawAddress(afterPayout.getPayoutRequest()));
		withdrawRequest.setValue(String.valueOf(afterPayout.getTargetAmount().divide(new BigDecimal(100))));
		withdrawRequest.setJettonSymbol(targetCurrency);
		withdrawRequest.setFromAddress(TonUtil.toRawAddress(afterPayout.getPublicKey()));
		return withdrawRequest;
	}

	private WithdrawRequest getWithdrawRequest(speed_core_cdc.speed_core_live.tbl_payout.Value afterPayout, String targetCurrency) {
		WithdrawRequest withdrawRequest = new WithdrawRequest();
		withdrawRequest.setToAddress(TonUtil.toRawAddress(afterPayout.getPayoutRequest()));
		withdrawRequest.setValue(String.valueOf(afterPayout.getTargetAmount().divide(new BigDecimal(100))));
		withdrawRequest.setJettonSymbol(targetCurrency);
		withdrawRequest.setFromAddress(TonUtil.toRawAddress(afterPayout.getPublicKey()));
		return withdrawRequest;
	}
}

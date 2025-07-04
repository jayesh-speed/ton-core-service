package com.speed.toncore.service;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.speed.javacommon.util.DateTimeUtil;
import com.speed.toncore.accounts.service.TransactionFeeService;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.domain.model.QWithdrawProcess;
import com.speed.toncore.domain.model.WithdrawProcess;
import com.speed.toncore.enums.WithdrawProcessStatus;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import com.speed.toncore.pojo.JettonTransferDto;
import com.speed.toncore.pojo.WithdrawProcessPojo;
import com.speed.toncore.repository.WithdrawProcessRepository;
import com.speed.toncore.ton.TonCoreServiceHelper;
import com.speed.toncore.util.TonUtils;
import com.speed.toncore.withdraw.request.WithdrawRequest;
import com.speed.toncore.withdraw.response.WithdrawResponse;
import com.speed.toncore.withdraw.service.WithdrawService;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WithdrawProcessHelper {

	private static final QWithdrawProcess qWithdrawProcess = QWithdrawProcess.withdrawProcess;
	private final WithdrawProcessRepository withdrawProcessRepository;
	private final WithdrawService withdrawService;
	private final TonCoreServiceHelper tonCoreServiceHelper;
	private final TransactionFeeService transactionFeeService;

	public void saveWithdrawProcess(WithdrawProcessPojo withdrawProcessPojo) {
		String accountId = withdrawProcessPojo.getAccountId();
		String referenceId = withdrawProcessPojo.getReferenceId();
		if (withdrawProcessExistByWithdrawId(accountId, referenceId)) {
			LOG.warn(String.format(Errors.WITHDRAW_PROCESS_EXIST, referenceId, accountId));
			throw new EntityExistsException(String.format(Errors.WITHDRAW_PROCESS_EXIST, referenceId, accountId));
		}
		WithdrawProcess withdrawProcess = new WithdrawProcess();
		withdrawProcess.setReferenceId(referenceId);
		withdrawProcess.setAccountId(accountId);
		withdrawProcess.setWithdrawRequest(withdrawProcessPojo.getWithdrawRequest());
		withdrawProcess.setTargetAmount(withdrawProcessPojo.getTargetAmount());
		withdrawProcess.setTargetCurrency(withdrawProcessPojo.getTargetCurrency());
		withdrawProcess.setStatus(WithdrawProcessStatus.UNPAID.name());
		withdrawProcess.setWithdrawType(withdrawProcessPojo.getWithdrawType());
		withdrawProcess.setReference(withdrawProcessPojo.getReferenceType());
		withdrawProcess.setMainNet(ExecutionContextUtil.getContext().isMainNet());
		withdrawProcessRepository.save(withdrawProcess);
	}

	public void markWithdrawProcessFailed(String errorMessage, String accountId, String withdrawId) {
		QWithdrawProcess qWithdrawProcess = QWithdrawProcess.withdrawProcess;
		Map<Path<?>, Object> fieldWithValue = HashMap.newHashMap(3);
		fieldWithValue.put(qWithdrawProcess.failureReason, errorMessage);
		fieldWithValue.put(qWithdrawProcess.status, WithdrawProcessStatus.FAILED.name());
		fieldWithValue.put(qWithdrawProcess.modified, DateTimeUtil.currentEpochMilliSecondsUTC());

		updateWithdrawProcessByWithdrawId(accountId, withdrawId, fieldWithValue);
	}

	private boolean withdrawProcessExistByWithdrawId(String accountId, String withdrawId) {
		return withdrawProcessRepository.exists(qWithdrawProcess.referenceId.eq(withdrawId).and(qWithdrawProcess.accountId.eq(accountId)));
	}

	public void updateWithdrawProcessByWithdrawId(String accountId, String withdrawId, Map<Path<?>, Object> fieldWithValue) {
		Predicate predicate = qWithdrawProcess.referenceId.eq(withdrawId).and(qWithdrawProcess.accountId.eq(accountId));
		withdrawProcessRepository.updateFields(predicate, qWithdrawProcess, fieldWithValue);
	}

	public void processWithdraw(WithdrawRequest withdrawRequest, String accountId, String withdrawId) {
		WithdrawResponse response;
		try {
			response = withdrawService.transferJetton(withdrawRequest);
		} catch (Exception ex) {
			LOG.error(Errors.ERROR_PROCESSING_WITHDRAW, ex);
			String errorMsg = ex.getMessage();
			markWithdrawProcessFailed(errorMsg, accountId, withdrawId);
			return;
		}
		updateTxHashInWithdrawProcess(accountId, withdrawId, response.getTxReference(), response.getFromAddress());
	}

	private void updateTxHashInWithdrawProcess(String accountId, String withdrawId, String txReference, String fromAddress) {
		Predicate predicate = qWithdrawProcess.referenceId.eq(withdrawId).and(qWithdrawProcess.accountId.eq(accountId));
		Map<Path<?>, Object> fieldWithValue = HashMap.newHashMap(3);
		Long currentTime = DateTimeUtil.currentEpochMilliSecondsUTC();
		fieldWithValue.put(qWithdrawProcess.txReference, txReference);
		fieldWithValue.put(qWithdrawProcess.address, fromAddress);
		fieldWithValue.put(qWithdrawProcess.modified, currentTime);
		withdrawProcessRepository.updateFields(predicate, qWithdrawProcess, fieldWithValue);
	}

	public void markWithdrawProcessPaid(JettonTransferDto transfer) {
		BigDecimal fees = transactionFeeService.getJettonTransactionFee(transfer.getTraceId());
		Long currentTime = DateTimeUtil.currentEpochMilliSecondsUTC();
		String txReference = TonUtils.deserializeTransactionReference(transfer.getForwardPayload());
		Predicate predicate = qWithdrawProcess.txReference.eq(txReference);
		Map<Path<?>, Object> fieldWithValue = HashMap.newHashMap(5);
		fieldWithValue.put(qWithdrawProcess.actualFee, fees);
		fieldWithValue.put(qWithdrawProcess.transactionHash, transfer.getTransactionHash());
		fieldWithValue.put(qWithdrawProcess.status, WithdrawProcessStatus.PAID.name());
		fieldWithValue.put(qWithdrawProcess.targetAmountPaidAt, currentTime);
		fieldWithValue.put(qWithdrawProcess.modified, currentTime);
		withdrawProcessRepository.updateFields(predicate, qWithdrawProcess, fieldWithValue);
	}
}

package com.speed.toncore.accounts.controller;

import com.speed.toncore.accounts.request.TransactionFeeRequest;
import com.speed.toncore.accounts.response.TransactionFeeResponse;
import com.speed.toncore.accounts.service.TransactionFeeService;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Endpoints;
import com.speed.toncore.constants.LogKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TransactionFeeController {

	private final TransactionFeeService transactionFeeService;

	@PostMapping(Endpoints.JETTON_TRANSFER_FEE)
	public ResponseEntity<TransactionFeeResponse> getJettonTransactionFee(@Valid @RequestBody TransactionFeeRequest request) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.JETTON_TRANSACTION_FEE);
		return ResponseEntity.ok(
				TransactionFeeResponse.builder().transactionFee(transactionFeeService.getJettonTransactionFee(request.getTraceId())).build());
	}

	@PostMapping(Endpoints.SWEEP_TRANSACTION_FEE)
	public ResponseEntity<TransactionFeeResponse> getSweepTransactionFee(@Valid @RequestBody TransactionFeeRequest request) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.SWEEP_TRANSACTION_FEE);
		return ResponseEntity.ok(
				TransactionFeeResponse.builder().transactionFee(transactionFeeService.getSweepTransactionFee(request.getTraceId())).build());
	}
}

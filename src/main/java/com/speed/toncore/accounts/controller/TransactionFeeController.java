package com.speed.toncore.accounts.controller;

import com.speed.toncore.accounts.request.FeeEstimationRequest;
import com.speed.toncore.accounts.request.TransactionFeeRequest;
import com.speed.toncore.accounts.response.EstimateFeeResponse;
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

	@PostMapping(Endpoints.TOKEN_TRANSFER_FEE)
	public ResponseEntity<TransactionFeeResponse> getTokenTransferFee(@Valid @RequestBody TransactionFeeRequest request) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.TOKEN_TRANSFER_FEE);
		return ResponseEntity.ok(
				TransactionFeeResponse.builder().transactionFee(transactionFeeService.getTokenTransferFee(request.getTraceId())).build());
	}

	@PostMapping(Endpoints.SWEEP_FEE)
	public ResponseEntity<TransactionFeeResponse> getSweepFee(@Valid @RequestBody TransactionFeeRequest request) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.SWEEP_FEE);
		return ResponseEntity.ok(TransactionFeeResponse.builder().transactionFee(transactionFeeService.getSweepFee(request.getTraceId())).build());
	}

	@PostMapping(Endpoints.ESTIMATE_TRANSACTION_FEE)
	public ResponseEntity<EstimateFeeResponse> estimateTransactionFee(@Valid @RequestBody FeeEstimationRequest request) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.ESTIMATE_TRANSACTION_FEE);
		return ResponseEntity.ok(transactionFeeService.estimateTransactionFee(request));
	}
}

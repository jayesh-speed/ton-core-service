package com.speed.toncore.accounts.controller;

import com.speed.toncore.accounts.response.DeployedAccountResponse;
import com.speed.toncore.accounts.response.TonAccountResponse;
import com.speed.toncore.accounts.service.TonFeeAccountService;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Endpoints;
import com.speed.toncore.constants.LogKeys;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class TonFeeAccountController {

	private final TonFeeAccountService tonFeeAccountService;

	@PostMapping(Endpoints.CREATE_FEE_ACCOUNT)
	public ResponseEntity<TonAccountResponse> createFeeAccount() {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.CREATE_FEE_ACCOUNT);
		return ResponseEntity.ok(tonFeeAccountService.createFeeAccount());
	}

	@DeleteMapping(Endpoints.REMOVE_FEE_ACCOUNT)
	public ResponseEntity<Void> removeFeeAccount(@PathVariable @NotBlank String address) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.REMOVE_FEE_ACCOUNT);
		tonFeeAccountService.deleteFeeAccount(address);
		return ResponseEntity.ok().build();
	}

	@PostMapping(Endpoints.DEPLOY_FEE_ACCOUNT)
	public ResponseEntity<DeployedAccountResponse> deployFeeAccount(@PathVariable @NotBlank String address) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.DEPLOY_FEE_ACCOUNT);
		return ResponseEntity.ok(tonFeeAccountService.deployFeeAccount(address));
	}

	@GetMapping(Endpoints.GET_FEE_ACCOUNTS)
	public ResponseEntity<List<TonAccountResponse>> getFeeAccounts() {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.GET_FEE_ACCOUNTS);
		return ResponseEntity.ok(tonFeeAccountService.getFeeAccounts());
	}
}

package com.speed.toncore.balance.controller;

import com.speed.toncore.accounts.response.BalanceResponse;
import com.speed.toncore.balance.service.BalanceService;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Endpoints;
import com.speed.toncore.constants.LogKeys;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class BalanceController {

	private final BalanceService balanceService;

	@GetMapping(Endpoints.GET_TON_BALANCE)
	public ResponseEntity<BalanceResponse> getTonBalance(@PathVariable @NotBlank String address) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.GET_TON_BALANCE);
		return ResponseEntity.ok(balanceService.getTonBalance(address));
	}

	@GetMapping(Endpoints.GET_TOKEN_BALANCE)
	public ResponseEntity<BalanceResponse> getTokenBalance(@PathVariable @NotBlank String tokenAddress, @PathVariable @NotBlank String ownerAddress) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.GET_TOKEN_BALANCE);
		return ResponseEntity.ok(balanceService.getTokenBalance(tokenAddress, ownerAddress));
	}
}

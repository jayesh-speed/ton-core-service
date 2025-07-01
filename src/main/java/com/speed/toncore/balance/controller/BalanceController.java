package com.speed.toncore.balance.controller;

import com.speed.toncore.accounts.response.BalanceResponse;
import com.speed.toncore.balance.service.BalanceService;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Endpoints;
import com.speed.toncore.constants.LogKeys;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BalanceController {

	private final BalanceService balanceService;

	@GetMapping(Endpoints.GET_TON_BALANCE)
	public ResponseEntity<BalanceResponse> getTonBalance(@PathVariable String address) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.GET_TON_BALANCE);
		return ResponseEntity.ok(balanceService.getTonBalance(address));
	}

	@GetMapping(Endpoints.GET_JETTON_BALANCE)
	public ResponseEntity<BalanceResponse> getJettonBalance(@PathVariable String jettonAddress, @PathVariable String accountAddress) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.GET_JETTON_BALANCE);
		return ResponseEntity.ok(balanceService.getJettonBalance(jettonAddress, accountAddress));
	}
}

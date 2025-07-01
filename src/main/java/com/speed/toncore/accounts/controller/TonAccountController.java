package com.speed.toncore.accounts.controller;

import com.speed.toncore.accounts.request.TonWalletRequest;
import com.speed.toncore.accounts.response.TonAccountResponse;
import com.speed.toncore.accounts.service.TonWalletService;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Endpoints;
import com.speed.toncore.constants.LogKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TonAccountController {

	private final TonWalletService tonWalletService;

	@GetMapping(Endpoints.GET_TON_WALLET_ADDRESS)
	public ResponseEntity<TonAccountResponse> getWalletAccount() {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.GET_TON_WALLET_ADDRESS);
		TonAccountResponse usedWalletAddress = tonWalletService.getNewWalletAddress();
		tonWalletService.checkAddressAvailabilityAndCreate();
		return ResponseEntity.ok(usedWalletAddress);
	}

	@PostMapping(Endpoints.CREATE_TON_WALLET_ADDRESSES)
	public ResponseEntity<Void> createTonAddresses(@RequestBody @Valid TonWalletRequest tonWalletRequest) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.CREATE_TON_WALLET_ADDRESSES);
		tonWalletService.createPoolOfTonWalletAddresses(tonWalletRequest);
		return ResponseEntity.ok().build();
	}

	@PutMapping(Endpoints.REMOVE_USED_TON_WALLET_ADDRESS)
	public ResponseEntity<Void> removeUsedTonWalletAddress(@PathVariable String address) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.REMOVE_USED_TON_WALLET_ADDRESS);
		tonWalletService.removeUsedTonWalletAddress(address);
		return ResponseEntity.ok().build();
	}
}
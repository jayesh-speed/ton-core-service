package com.speed.toncore.accounts.controller;

import com.speed.toncore.accounts.request.TonAddressRequest;
import com.speed.toncore.accounts.response.TonAccountResponse;
import com.speed.toncore.accounts.service.TonAddressService;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Endpoints;
import com.speed.toncore.constants.LogKeys;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class TonAccountController {

	private final TonAddressService tonAddressService;

	@GetMapping(Endpoints.GET_TON_ADDRESS)
	public ResponseEntity<TonAccountResponse> getNewTonAddress() {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.GET_TON_ADDRESS);
		TonAccountResponse usedTonAddress = tonAddressService.getNewTonAddress();
		tonAddressService.checkAddressAvailabilityAndCreate();
		return ResponseEntity.ok(usedTonAddress);
	}

	@PostMapping(Endpoints.CREATE_TON_ADDRESSES)
	public ResponseEntity<Void> createTonAddresses(@RequestBody @Valid TonAddressRequest tonAddressRequest) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.CREATE_TON_ADDRESSES);
		tonAddressService.createPoolOfTonAddresses(tonAddressRequest);
		return ResponseEntity.ok().build();
	}

	@PutMapping(Endpoints.REMOVE_USED_TON_ADDRESS)
	public ResponseEntity<Void> removeUsedTonAddress(@PathVariable @NotBlank String address) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.REMOVE_USED_TON_ADDRESS);
		tonAddressService.removeUsedTonAddress(address);
		return ResponseEntity.ok().build();
	}
}
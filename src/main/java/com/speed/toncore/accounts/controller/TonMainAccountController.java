package com.speed.toncore.accounts.controller;

import com.speed.toncore.accounts.request.TonMainAccountRequest;
import com.speed.toncore.accounts.response.DeployedAccountResponse;
import com.speed.toncore.accounts.response.TonAccountResponse;
import com.speed.toncore.accounts.service.TonMainAccountService;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Endpoints;
import com.speed.toncore.constants.LogKeys;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class TonMainAccountController {

	private final TonMainAccountService tonMainAccountService;

	@PostMapping(Endpoints.CREATE_MAIN_ACCOUNT)
	public ResponseEntity<TonAccountResponse> createMainAccount(@Valid @RequestBody TonMainAccountRequest tonMainAccountRequest) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.CREATE_MAIN_ACCOUNT);
		TonAccountResponse mainAccount = tonMainAccountService.createMainAccount(tonMainAccountRequest.getJettonMasterAddress());
		tonMainAccountService.publishMainAccountCreateEvent();
		return ResponseEntity.ok(mainAccount);
	}

	@DeleteMapping(Endpoints.REMOVE_MAIN_ACCOUNT)
	public ResponseEntity<Void> removeMainAccount(@PathVariable @NotBlank String address) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.REMOVE_MAIN_ACCOUNT);
		tonMainAccountService.deleteMainAccount(address);
		return ResponseEntity.ok().build();
	}

	@PutMapping(Endpoints.UPDATE_JETTON_WALLET)
	public ResponseEntity<Void> updateJettonWallet(@PathVariable @NotBlank String address) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.UPDATE_JETTON_WALLET);
		tonMainAccountService.addMainAccountJettonWallet(address);
		return ResponseEntity.ok().build();
	}

	@PostMapping(Endpoints.DEPLOY_MAIN_ACCOUNT)
	public ResponseEntity<DeployedAccountResponse> deployMainAccount(@PathVariable @NotBlank String address) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.DEPLOY_MAIN_ACCOUNT);
		return ResponseEntity.ok(tonMainAccountService.deployMainAccount(address));
	}

	@GetMapping(Endpoints.GET_MAIN_ACCOUNTS)
	public ResponseEntity<List<TonAccountResponse>> getMainAccounts(@PathVariable @NotBlank String jettonMasterAddress) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.GET_MAIN_ACCOUNT);
		return ResponseEntity.ok(tonMainAccountService.getMainAccounts(jettonMasterAddress));
	}
}

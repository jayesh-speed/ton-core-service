package com.speed.toncore.jettons.controller;

import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Endpoints;
import com.speed.toncore.constants.LogKeys;
import com.speed.toncore.jettons.request.TonJettonRequest;
import com.speed.toncore.jettons.response.TonJettonResponse;
import com.speed.toncore.jettons.service.TonJettonService;
import com.speed.toncore.util.TonUtil;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class TonJettonController {

	private final TonJettonService tonJettonService;

	@PostMapping(Endpoints.ADD_TON_JETTON)
	public ResponseEntity<TonJettonResponse> addNewTonJetton(@Valid @RequestBody TonJettonRequest tonJettonRequest) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.ADD_TON_JETTON);
		return ResponseEntity.ok(tonJettonService.addNewTonJetton(tonJettonRequest));
	}

	@DeleteMapping(Endpoints.REMOVE_TON_JETTON)
	public ResponseEntity<Void> removeTonJetton(@PathVariable @NotBlank String address) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.REMOVE_TON_JETTON);
		tonJettonService.deleteTonJetton(TonUtil.toRawAddress(address));
		return ResponseEntity.ok().build();
	}

	@GetMapping(Endpoints.GET_ALL_JETTONS)
	public ResponseEntity<List<TonJettonResponse>> getAllTonJettons() {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.GET_ALL_JETTONS);
		return ResponseEntity.ok(tonJettonService.getAllJettons());
	}

	@GetMapping(Endpoints.GET_JETTON_BY_SYMBOL)
	public ResponseEntity<TonJettonResponse> getTonJettonBySymbol(@PathVariable @NotBlank String symbol) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.GET_JETTON_BY_SYMBOL);
		return ResponseEntity.ok(tonJettonService.getTonJettonBySymbol(symbol));
	}

	@GetMapping(Endpoints.GET_JETTON)
	public ResponseEntity<TonJettonResponse> getTonJetton(@PathVariable @NotBlank String address) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.GET_JETTON);
		return ResponseEntity.ok(tonJettonService.getTonJettonByAddress(TonUtil.toRawAddress(address)));
	}
}

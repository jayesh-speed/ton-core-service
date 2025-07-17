package com.speed.toncore.tokens.controller;

import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Endpoints;
import com.speed.toncore.constants.LogKeys;
import com.speed.toncore.tokens.request.TonTokenRequest;
import com.speed.toncore.tokens.response.TonTokenResponse;
import com.speed.toncore.tokens.service.TonTokenService;
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
public class TonTokenController {

	private final TonTokenService tonTokenService;

	@PostMapping(Endpoints.ADD_TON_TOKEN)
	public ResponseEntity<TonTokenResponse> addNewTonToken(@Valid @RequestBody TonTokenRequest tonTokenRequest) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.ADD_TON_TOKEN);
		return ResponseEntity.ok(tonTokenService.addNewTonToken(tonTokenRequest));
	}

	@DeleteMapping(Endpoints.REMOVE_TON_TOKEN)
	public ResponseEntity<Void> removeTonToken(@PathVariable @NotBlank String address) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.REMOVE_TON_TOKEN);
		tonTokenService.deleteTonToken(TonUtil.toRawAddress(address));
		return ResponseEntity.ok().build();
	}

	@GetMapping(Endpoints.GET_ALL_TOKENS)
	public ResponseEntity<List<TonTokenResponse>> getAllTonTokens() {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.GET_ALL_TOKENS);
		return ResponseEntity.ok(tonTokenService.getAllTokens());
	}

	@GetMapping(Endpoints.GET_TOKEN_BY_SYMBOL)
	public ResponseEntity<TonTokenResponse> getTonTokenBySymbol(@PathVariable @NotBlank String symbol) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.GET_TOKEN_BY_SYMBOL);
		return ResponseEntity.ok(tonTokenService.getTonTokenBySymbol(symbol));
	}

	@GetMapping(Endpoints.GET_TOKENS)
	public ResponseEntity<TonTokenResponse> getTonToken(@PathVariable @NotBlank String address) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.GET_TOKEN);
		return ResponseEntity.ok(tonTokenService.getTonTokenByAddress(TonUtil.toRawAddress(address)));
	}
}

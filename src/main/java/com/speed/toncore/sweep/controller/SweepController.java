package com.speed.toncore.sweep.controller;

import com.speed.toncore.constants.Endpoints;
import com.speed.toncore.sweep.request.SweepRequest;
import com.speed.toncore.sweep.response.SweepResponse;
import com.speed.toncore.sweep.service.SweepService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SweepController {

	private final SweepService sweepService;

	@PostMapping(Endpoints.INITIATE_SWEEP)
	public ResponseEntity<SweepResponse> performSweep(@RequestBody @Valid SweepRequest sweepRequest, @PathVariable String id) {
		return ResponseEntity.ok(sweepService.createSweepOnChainTx(sweepRequest, id));
	}
}

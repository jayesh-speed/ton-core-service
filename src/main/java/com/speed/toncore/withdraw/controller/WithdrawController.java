package com.speed.toncore.withdraw.controller;

import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Endpoints;
import com.speed.toncore.constants.LogKeys;
import com.speed.toncore.withdraw.request.WithdrawRequest;
import com.speed.toncore.withdraw.response.WithdrawResponse;
import com.speed.toncore.withdraw.service.WithdrawService;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WithdrawController {

	private final WithdrawService withdrawService;

	@PostMapping(Endpoints.TRANSFER_TOKEN)
	public ResponseEntity<WithdrawResponse> transferToken(@RequestBody @Validated WithdrawRequest withdrawRequest) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.TRANSFER_TOKEN);
		return ResponseEntity.ok(withdrawService.transferToken(withdrawRequest));
	}

	@PutMapping(Endpoints.UPDATE_LOGICAL_TIME)
	public ResponseEntity<Void> updateLatestLogicalTime(@PathVariable String id, @PathVariable Long logicalTime) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.UPDATE_LOGICAL_TIME);
		withdrawService.updateLatestLogicalTime(id, logicalTime);
		return ResponseEntity.ok().build();
	}
}

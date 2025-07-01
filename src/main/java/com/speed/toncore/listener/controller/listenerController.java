package com.speed.toncore.listener.controller;

import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Endpoints;
import com.speed.toncore.constants.LogKeys;
import com.speed.toncore.enums.TonListenerStatus;
import com.speed.toncore.listener.service.TonListenerService;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class listenerController {

	private final TonListenerService tonListenerService;

	@PostMapping(Endpoints.CREATE_TON_LISTENER)
	public ResponseEntity<Void> createTonListener() {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.CREATE_TON_LISTENER);
		tonListenerService.createTonListener();
		return ResponseEntity.ok().build();
	}

	@DeleteMapping(Endpoints.REMOVE_TON_LISTENER_BY_ID)
	public ResponseEntity<Void> removeTonListenerById(@PathVariable String id) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.REMOVE_TON_LISTENER);
		tonListenerService.deleteListenerById(id);
		return ResponseEntity.ok().build();
	}

	@PutMapping(Endpoints.UPDATE_TON_LISTENER)
	public ResponseEntity<Void> updateTonListener(@PathVariable String id) {
		MDC.put(LogKeys.EVENT_NAME, Constants.Events.UPDATE_TON_LISTENER_STATUS);
		tonListenerService.updateListenerStatus(tonListenerService.getListenerById(id), TonListenerStatus.IDLE.name());
		return ResponseEntity.ok().build();
	}
}

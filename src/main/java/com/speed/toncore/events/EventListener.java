package com.speed.toncore.events;

import com.speed.toncore.domain.model.TonListener;
import com.speed.toncore.enums.TonListenerStatus;
import com.speed.toncore.listener.service.TonListenerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EventListener {

	private final TonListenerService listenerService;

	@org.springframework.context.event.EventListener
	public void handleEoaAddressCreatedEvent(TonAddressCreatedEvent tonAddressCreatedEvent) {
		TonListener listener = listenerService.getListener();
		boolean equals = listener.getStatus().equals(TonListenerStatus.IDLE.name());
		if (equals) {
			long count = listenerService.updateListenerStatus(listener, TonListenerStatus.RUNNING.name());
			if (count == 0) {
				return;
			}
		}
		listenerService.stopAndDisposeListener(List.of(listener));
		listenerService.subscribeListener(listener);
	}

	@org.springframework.context.event.EventListener
	public void handleContextClosedEvent(ContextClosedEvent event) {
		List<TonListener> allListeners = listenerService.fetchAllListeners();
		allListeners.forEach(listener -> {
			listenerService.updateListenerStatus(listener, TonListenerStatus.IDLE.name());
		});
	}

	@org.springframework.context.event.EventListener
	public void handleTokenAddedEvent(TonJettonAddedEvent jettonAddedEvent) {
		TonListener listener = listenerService.getListener();
		boolean equals = listener.getStatus().equals(TonListenerStatus.IDLE.name());
		if (equals) {
			long count = listenerService.updateListenerStatus(listener, TonListenerStatus.RUNNING.name());
			if (count == 0) {
				return;
			}
		}
		listenerService.stopAndDisposeListener(List.of(listener));
		listenerService.subscribeListener(listener);
	}
}

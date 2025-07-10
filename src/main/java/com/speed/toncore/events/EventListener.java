package com.speed.toncore.events;

import com.speed.toncore.accounts.service.TonWalletService;
import com.speed.toncore.domain.model.TonListener;
import com.speed.toncore.enums.TonListenerStatus;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import com.speed.toncore.listener.service.TonListenerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventListener {

	private final TonListenerService listenerService;
	private final TonWalletService tonWalletService;

	@org.springframework.context.event.EventListener
	public void handleEoaAddressCreatedEvent(TonAddressCreatedEvent tonAddressCreatedEvent) {
		TonListener listener = getListenerAndUpdateToRunning();
		if (Objects.nonNull(listener)) {
			Integer chainId = ExecutionContextUtil.getContext().getChainId();
			listenerService.stopAndDisposeListener(listener);
			tonWalletService.clearReceiveAddressesCache(chainId);
			Set<String> receivingAddresses = tonWalletService.fetchReceiveAddresses(chainId);
			listenerService.subscribeListener(listener);
		}
	}

	@org.springframework.context.event.EventListener
	public void handleMainAccountCreatedEvent(MainAccountCreateEvent mainAccountCreateEvent) {
		TonListener listener = getListenerAndUpdateToRunning();
		if (Objects.nonNull(listener)) {
			Integer chainId = ExecutionContextUtil.getContext().getChainId();
			listenerService.stopAndDisposeListener(listener);
			tonWalletService.clearSendAddressesCache(chainId);
			Set<String> sendAddresses = tonWalletService.fetchSendAddresses(chainId);
			listenerService.subscribeListener(listener);
		}
	}

	@org.springframework.context.event.EventListener
	public void handleContextClosedEvent(ContextClosedEvent event) {
		List<TonListener> allListeners = listenerService.fetchAllListeners();
		allListeners.forEach(listener -> listenerService.updateListenerStatus(listener, TonListenerStatus.IDLE.name()));
	}

	@org.springframework.context.event.EventListener
	public void handleJettonAddedEvent(TonJettonAddedEvent jettonAddedEvent) {
		TonListener listener = getListenerAndUpdateToRunning();
		if (Objects.nonNull(listener)) {
			listenerService.stopAndDisposeListener(listener);
			listenerService.subscribeListener(listener);
		}
	}

	private TonListener getListenerAndUpdateToRunning() {
		TonListener listener = listenerService.getListener();
		boolean equals = listener.getStatus().equals(TonListenerStatus.IDLE.name());
		if (equals) {
			long count = listenerService.updateListenerStatus(listener, TonListenerStatus.RUNNING.name());
			if (count == 0) {
				return null;
			}
		}
		return listener;
	}
}

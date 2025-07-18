package com.speed.toncore.events;

import com.speed.toncore.accounts.service.TonAddressService;
import com.speed.toncore.domain.model.TonListener;
import com.speed.toncore.enums.TonListenerStatus;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import com.speed.toncore.listener.service.TonListenerService;
import com.speed.toncore.listener.service.impl.TonListenerHelper;
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
	private final TonAddressService tonAddressService;
	private final TonListenerHelper tonListenerHelper;

	@org.springframework.context.event.EventListener
	public void handleTonAddressCreatedEvent(TonAddressCreatedEvent tonAddressCreatedEvent) {
		TonListener listener = tonListenerHelper.fetchListenerAndUpdateToRunning();
		if (Objects.nonNull(listener)) {
			Integer chainId = ExecutionContextUtil.getContext().getChainId();
			listenerService.stopAndDisposeListener(listener);
			tonAddressService.clearReceiveAddressesCache(chainId);
			Set<String> receivingAddresses = tonAddressService.fetchReceiveAddresses(chainId);
			listenerService.subscribeListener(listener);
		}
	}

	@org.springframework.context.event.EventListener
	public void handleMainAccountCreatedEvent(MainAccountCreateEvent mainAccountCreateEvent) {
		TonListener listener = tonListenerHelper.fetchListenerAndUpdateToRunning();
		if (Objects.nonNull(listener)) {
			Integer chainId = ExecutionContextUtil.getContext().getChainId();
			listenerService.stopAndDisposeListener(listener);
			tonAddressService.clearSendAddressesCache(chainId);
			Set<String> sendAddresses = tonAddressService.fetchSendAddresses(chainId);
			listenerService.subscribeListener(listener);
		}
	}

	@org.springframework.context.event.EventListener
	public void handleContextClosedEvent(ContextClosedEvent event) {
		List<TonListener> allListeners = listenerService.fetchAllListeners();
		allListeners.forEach(listener -> listenerService.updateListenerStatus(listener, TonListenerStatus.IDLE.name()));
	}

	@org.springframework.context.event.EventListener
	public void handleTokenAddedEvent(TonTokenAddedEvent tokenAddedEvent) {
		TonListener listener = tonListenerHelper.fetchListenerAndUpdateToRunning();
		if (Objects.nonNull(listener)) {
			listenerService.stopAndDisposeListener(listener);
			listenerService.subscribeListener(listener);
		}
	}
}

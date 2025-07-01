package com.speed.toncore.listener.service;

import com.speed.toncore.domain.model.TonListener;

import java.util.List;

public interface TonListenerService {

	void createTonListener();

	void deleteListenerById(String id);

	long updateListenerStatus(TonListener listener, String status);

	TonListener getListenerById(String id);

	TonListener getListener();

	void subscribeListener(TonListener listener);

	void stopAndDisposeListener(TonListener listener);

	List<TonListener> fetchAllListeners();

	void bootUpTonListeners(boolean initialBootUp);
}

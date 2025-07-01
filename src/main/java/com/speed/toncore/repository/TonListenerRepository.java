package com.speed.toncore.repository;

import com.speed.javacommon.repository.SpeedCustomRepository;
import com.speed.toncore.domain.model.TonListener;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface TonListenerRepository extends SpeedCustomRepository<TonListener, String> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	TonListener findByChainId(Integer chainId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	TonListener findByChainIdAndStatus(Integer chainId, String status);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<TonListener> findById(String id);
}

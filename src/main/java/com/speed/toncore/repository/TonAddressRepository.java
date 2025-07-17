package com.speed.toncore.repository;

import com.speed.javacommon.repository.SpeedCustomRepository;
import com.speed.toncore.domain.model.TonAddress;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

@Repository
public interface TonAddressRepository extends SpeedCustomRepository<TonAddress, String> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	TonAddress findFirstByChainIdOrderByIdAsc(Integer chainId);
}

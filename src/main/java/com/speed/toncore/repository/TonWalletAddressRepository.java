package com.speed.toncore.repository;

import com.speed.javacommon.repository.SpeedCustomRepository;
import com.speed.toncore.domain.model.TonWalletAddress;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

@Repository
public interface TonWalletAddressRepository extends SpeedCustomRepository<TonWalletAddress, String> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	TonWalletAddress findFirstByChainIdOrderByIdAsc(Integer chainId);
}

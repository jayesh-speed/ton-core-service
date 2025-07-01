package com.speed.toncore.repository;

import com.speed.javacommon.repository.SpeedCustomRepository;
import com.speed.toncore.domain.model.TonOnChainTx;

public interface OnChainTxRepository extends SpeedCustomRepository<TonOnChainTx, String>,CustomOnChainTransactionRepository {}

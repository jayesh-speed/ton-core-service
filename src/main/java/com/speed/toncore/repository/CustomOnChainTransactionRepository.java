package com.speed.toncore.repository;

import com.querydsl.core.types.Predicate;
import com.speed.toncore.domain.model.TonOnChainTx;

public interface CustomOnChainTransactionRepository {
	TonOnChainTx getMaxLogicalTimeByPredicate(Predicate predicate);
}

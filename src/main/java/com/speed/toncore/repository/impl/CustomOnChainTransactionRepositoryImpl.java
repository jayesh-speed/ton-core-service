package com.speed.toncore.repository.impl;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.speed.toncore.domain.model.QTonOnChainTx;
import com.speed.toncore.domain.model.TonOnChainTx;
import com.speed.toncore.repository.CustomOnChainTransactionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomOnChainTransactionRepositoryImpl implements CustomOnChainTransactionRepository {

	private static final QTonOnChainTx qTonOnChainTx = QTonOnChainTx.tonOnChainTx;
	@PersistenceContext
	private final EntityManager entityManager;

	@Override
	public TonOnChainTx getMaxLogicalTimeByPredicate(Predicate predicate) {
		return new JPAQuery<>(entityManager).select(Projections.fields(qTonOnChainTx, qTonOnChainTx.logicalTime))
				.where(predicate)
				.from(qTonOnChainTx)
				.orderBy(qTonOnChainTx.logicalTime.desc())
				.fetchFirst();
	}
}

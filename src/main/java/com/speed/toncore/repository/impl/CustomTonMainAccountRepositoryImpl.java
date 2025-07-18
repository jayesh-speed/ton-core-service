package com.speed.toncore.repository.impl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.speed.toncore.domain.model.QTonMainAccount;
import com.speed.toncore.domain.model.TonMainAccount;
import com.speed.toncore.repository.CustomTonMainAccountRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomTonMainAccountRepositoryImpl implements CustomTonMainAccountRepository {

	private static final QTonMainAccount qTonMainAccount = QTonMainAccount.tonMainAccount;
	@PersistenceContext
	private final EntityManager entityManager;

	@Override
	public TonMainAccount findMainAccountByPredicateAndOrder(Predicate predicate, OrderSpecifier<?> orderSpecifier) {
		return new JPAQuery<TonMainAccount>(entityManager).from(qTonMainAccount).where(predicate).orderBy(orderSpecifier).fetchFirst();
	}
}

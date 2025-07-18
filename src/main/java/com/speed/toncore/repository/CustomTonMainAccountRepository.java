package com.speed.toncore.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.speed.toncore.domain.model.TonMainAccount;

public interface CustomTonMainAccountRepository {

	TonMainAccount findMainAccountByPredicateAndOrder(Predicate predicate, OrderSpecifier<?> orderSpecifier);
}

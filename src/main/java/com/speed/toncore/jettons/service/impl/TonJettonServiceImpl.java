package com.speed.toncore.jettons.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.speed.javacommon.exceptions.BadRequestException;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.domain.model.QTonJetton;
import com.speed.toncore.domain.model.TonJetton;
import com.speed.toncore.events.TonJettonAddedEvent;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import com.speed.toncore.jettons.request.TonJettonRequest;
import com.speed.toncore.jettons.response.TonJettonResponse;
import com.speed.toncore.jettons.service.TonJettonService;
import com.speed.toncore.mapper.TonJettonMapper;
import com.speed.toncore.repository.TonJettonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TonJettonServiceImpl implements TonJettonService {

	private static final QTonJetton qTonJetton = QTonJetton.tonJetton;
	private final TonJettonRepository tonJettonRepository;
	private final ApplicationEventPublisher eventPublisher;

	@Override
	public TonJettonResponse addNewTonJetton(TonJettonRequest tonJettonRequest) {
		TonJetton tonJetton = TonJettonMapper.INSTANCE.mapJettonRequestToModal(tonJettonRequest);
		tonJetton.setChainId(ExecutionContextUtil.getContext().getChainId());
		tonJetton.setMainNet(ExecutionContextUtil.getContext().isMainNet());
		Predicate queryPredicate = new BooleanBuilder(qTonJetton.jettonMasterAddress.eq(tonJetton.getJettonMasterAddress())).and(
				qTonJetton.chainId.eq(ExecutionContextUtil.getContext().getChainId()));
		if (tonJettonRepository.exists(queryPredicate)) {
			throw new BadRequestException(String.format(Errors.JETTON_ALREADY_EXISTS, tonJetton.getJettonMasterAddress(), tonJetton.getChainId()), null,
					null);
		}
		tonJettonRepository.save(tonJetton);
		TonJettonAddedEvent tokenAddedEvent = new TonJettonAddedEvent();
		eventPublisher.publishEvent(tokenAddedEvent);
		return TonJettonMapper.INSTANCE.mapModalToJettonResponse(tonJetton);
	}

	@Override
	@CacheEvict(value = Constants.CacheNames.JETTON_RESPONSE, keyGenerator = Constants.CACHE_KEY_GENERATOR)
	public void deleteTonJetton(String address) {
		Predicate queryPredicate = new BooleanBuilder(qTonJetton.jettonMasterAddress.eq(address)).and(
				qTonJetton.chainId.eq(ExecutionContextUtil.getContext().getChainId()));
		tonJettonRepository.deleteByPredicate(queryPredicate, qTonJetton);
	}

	@Override
	public List<TonJettonResponse> getAllJettons() {
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
		BooleanBuilder predicateQuery = new BooleanBuilder(qTonJetton.chainId.eq(chainId));
		List<TonJetton> tonJettons = tonJettonRepository.findAndProject(predicateQuery, qTonJetton, qTonJetton.jettonMasterAddress,
				qTonJetton.jettonName, qTonJetton.id, qTonJetton.mainNet, qTonJetton.chainId, qTonJetton.decimals, qTonJetton.jettonSymbol,
				qTonJetton.forwardTonAmount);
		List<TonJettonResponse> responseList = new ArrayList<>();
		tonJettons.forEach(token -> responseList.add(TonJettonMapper.INSTANCE.mapModalToJettonResponse(token)));
		return responseList;
	}

	@Override
	@Cacheable(value = Constants.CacheNames.JETTON_RESPONSE, keyGenerator = Constants.CACHE_KEY_GENERATOR)
	public TonJettonResponse getTonJettonBySymbol(String jettonSymbol) {
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
		Predicate queryPredicate = new BooleanBuilder(qTonJetton.chainId.eq(chainId)).and(qTonJetton.jettonSymbol.eq(jettonSymbol));
		TonJetton ethToken = tonJettonRepository.findAndProjectUnique(queryPredicate, qTonJetton, qTonJetton.id, qTonJetton.chainId,
				qTonJetton.jettonMasterAddress, qTonJetton.jettonName, qTonJetton.jettonSymbol, qTonJetton.decimals, qTonJetton.forwardTonAmount);
		return TonJettonMapper.INSTANCE.mapModalToJettonResponse(ethToken);
	}

	@Override
	@Cacheable(value = Constants.CacheNames.JETTON_RESPONSE, keyGenerator = Constants.CACHE_KEY_GENERATOR)
	public TonJettonResponse getTonJettonByAddress(String jettonMasterAddress) {
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
		Predicate predicateQuery = new BooleanBuilder(qTonJetton.chainId.eq(chainId)).and(qTonJetton.jettonMasterAddress.eq(jettonMasterAddress));
		TonJetton tonJetton = tonJettonRepository.findAndProjectUnique(predicateQuery, qTonJetton, qTonJetton.id, qTonJetton.chainId,
				qTonJetton.jettonMasterAddress, qTonJetton.jettonName, qTonJetton.jettonSymbol, qTonJetton.mainNet, qTonJetton.decimals,
				qTonJetton.forwardTonAmount);
		return TonJettonMapper.INSTANCE.mapModalToJettonResponse(tonJetton);
	}
}

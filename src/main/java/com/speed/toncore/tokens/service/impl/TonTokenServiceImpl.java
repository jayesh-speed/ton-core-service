package com.speed.toncore.tokens.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.speed.javacommon.exceptions.BadRequestException;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.domain.model.QTonToken;
import com.speed.toncore.domain.model.TonToken;
import com.speed.toncore.events.TonTokenAddedEvent;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import com.speed.toncore.mapper.TonTokenMapper;
import com.speed.toncore.repository.TonTokenRepository;
import com.speed.toncore.tokens.request.TonTokenRequest;
import com.speed.toncore.tokens.response.TonTokenResponse;
import com.speed.toncore.tokens.service.TonTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TonTokenServiceImpl implements TonTokenService {

	private static final QTonToken qTonToken = QTonToken.tonToken;
	private final TonTokenRepository tonTokenRepository;
	private final ApplicationEventPublisher eventPublisher;

	@Override
	public TonTokenResponse addNewTonToken(TonTokenRequest tonTokenRequest) {
		TonToken tonToken = TonTokenMapper.INSTANCE.mapTokenRequestToModal(tonTokenRequest);
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
		tonToken.setChainId(chainId);
		tonToken.setMainNet(ExecutionContextUtil.getContext().isMainNet());
		Predicate queryPredicate = new BooleanBuilder(qTonToken.tokenAddress.eq(tonToken.getTokenAddress())).and(qTonToken.chainId.eq(chainId));
		if (tonTokenRepository.exists(queryPredicate)) {
			throw new BadRequestException(String.format(Errors.TOKEN_ALREADY_EXISTS, tonToken.getTokenAddress(), tonToken.getChainId()), null, null);
		}
		tonTokenRepository.save(tonToken);
		eventPublisher.publishEvent(new TonTokenAddedEvent());
		return TonTokenMapper.INSTANCE.mapModalToTokenResponse(tonToken);
	}

	@Override
	@CacheEvict(value = Constants.CacheNames.TOKEN_RESPONSE, keyGenerator = Constants.CACHE_KEY_GENERATOR)
	public void deleteTonToken(String address) {
		Predicate queryPredicate = new BooleanBuilder(qTonToken.tokenAddress.eq(address)).and(
				qTonToken.chainId.eq(ExecutionContextUtil.getContext().getChainId()));
		tonTokenRepository.deleteByPredicate(queryPredicate, qTonToken);
	}

	@Override
	public List<TonTokenResponse> getAllTokens() {
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
		Predicate predicateQuery = new BooleanBuilder(qTonToken.chainId.eq(chainId));
		List<TonToken> tonTokens = tonTokenRepository.findAndProject(predicateQuery, qTonToken, qTonToken.tokenAddress, qTonToken.tokenName,
				qTonToken.id, qTonToken.mainNet, qTonToken.chainId, qTonToken.decimals, qTonToken.tokenSymbol, qTonToken.noOfCell, qTonToken.noOfBits,
				qTonToken.gasUnit, qTonToken.deploymentCost, qTonToken.reserveStorageFee, qTonToken.noOfCellV3, qTonToken.noOfBitsV3,
				qTonToken.gasUnitV3);
		List<TonTokenResponse> responseList = new ArrayList<>();
		tonTokens.forEach(token -> responseList.add(TonTokenMapper.INSTANCE.mapModalToTokenResponse(token)));
		return responseList;
	}

	@Override
	@Cacheable(value = Constants.CacheNames.TOKEN_RESPONSE, keyGenerator = Constants.CACHE_KEY_GENERATOR)
	public TonTokenResponse getTonTokenBySymbol(String tokenSymbol) {
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
		Predicate queryPredicate = new BooleanBuilder(qTonToken.chainId.eq(chainId)).and(qTonToken.tokenSymbol.eq(tokenSymbol));
		TonToken tonToken = tonTokenRepository.findAndProjectUnique(queryPredicate, qTonToken, qTonToken.id, qTonToken.chainId, qTonToken.tokenAddress,
				qTonToken.tokenName, qTonToken.tokenSymbol, qTonToken.decimals, qTonToken.noOfCell, qTonToken.noOfBits, qTonToken.gasUnit,
				qTonToken.deploymentCost, qTonToken.reserveStorageFee, qTonToken.noOfCellV3, qTonToken.noOfBitsV3, qTonToken.gasUnitV3);
		return TonTokenMapper.INSTANCE.mapModalToTokenResponse(tonToken);
	}

	@Override
	@Cacheable(value = Constants.CacheNames.TOKEN_RESPONSE, keyGenerator = Constants.CACHE_KEY_GENERATOR)
	public TonTokenResponse getTonTokenByAddress(String tokenAddress) {
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
		Predicate predicateQuery = new BooleanBuilder(qTonToken.chainId.eq(chainId)).and(qTonToken.tokenAddress.eq(tokenAddress));
		TonToken tonToken = tonTokenRepository.findAndProjectUnique(predicateQuery, qTonToken, qTonToken.id, qTonToken.chainId, qTonToken.tokenAddress,
				qTonToken.tokenName, qTonToken.tokenSymbol, qTonToken.mainNet, qTonToken.decimals, qTonToken.noOfCell, qTonToken.noOfBits,
				qTonToken.gasUnit, qTonToken.deploymentCost, qTonToken.reserveStorageFee, qTonToken.noOfCellV3, qTonToken.noOfBitsV3,
				qTonToken.gasUnitV3);
		return TonTokenMapper.INSTANCE.mapModalToTokenResponse(tonToken);
	}
}

package com.speed.toncore.ton;

import com.speed.javacommon.exceptions.BadRequestException;
import com.speed.javacommon.util.Assert;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Component
public class TonNodePool {

	private final Map<Long, TonNode> chainIdTonNodeMap = new ConcurrentHashMap<>(2);

	private final List<Integer> supportedChainIds = Arrays.asList(Constants.TEST_NET_CHAIN_ID, Constants.MAIN_NET_CHAIN_ID);

	public TonNode getTonNodeByChainId() {
		Integer chainId = ExecutionContextUtil.getContext().getChainId();
		Assert.nonNull(chainId, () -> new BadRequestException(String.format(Errors.MISSING_HEADER, Constants.CHAIN_ID), null, null));
		return getTonNodeByChainId(chainId);
	}

	private TonNode getTonNodeByChainId(long chainId) {
		TonNode tonNode = chainIdTonNodeMap.get(chainId);
		Assert.nonNull(tonNode, () -> new BadRequestException(String.format(Errors.NODE_NOT_FOUND, chainId), null, null));
		return tonNode;
	}

	void addTonNodeAndChainId(long chainId, TonNode tonNode) {
		chainIdTonNodeMap.put(chainId, tonNode);
	}

	public void removeTonNodeAndChainId(long chainId) {
		chainIdTonNodeMap.remove(chainId);
	}

	public List<Integer> getSupportedChainIds() {
		List<Integer> chainIds = new ArrayList<>(supportedChainIds.size());
		chainIds.addAll(supportedChainIds);
		return chainIds;
	}
}

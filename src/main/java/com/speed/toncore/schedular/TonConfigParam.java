package com.speed.toncore.schedular;

import com.speed.javacommon.util.RequestIdGenerator;
import com.speed.javacommon.util.StringUtil;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import com.speed.toncore.pojo.TonConfigParamDto;
import com.speed.toncore.ton.TonCoreServiceHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.ton.ton4j.cell.Cell;
import org.ton.ton4j.cell.CellBuilder;
import org.ton.ton4j.cell.CellSlice;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class TonConfigParam {

	private static final List<Integer> CONFIG_PARAMS = List.of(21, 25);
	private static final List<Integer> SUPPORTED_CHAIN_IDS = List.of(Constants.MAIN_NET_CHAIN_ID, Constants.TEST_NET_CHAIN_ID);

	private static final Map<Integer, ConfigParam> TON_CONFIG = new ConcurrentHashMap<>(2);

	static {
		SUPPORTED_CHAIN_IDS.forEach(chainId -> TON_CONFIG.put(chainId, new ConfigParam()));
	}

	private final TonCoreServiceHelper tonCoreServiceHelper;

	public static ConfigParam getConfigByChainId(Integer chainId) {
		return TON_CONFIG.get(chainId);
	}

	public void initConfigParam(Integer chainId) {
		updateConfigParam(chainId);
	}

	@Scheduled(cron = "0 0 0 * * *", zone = "UTC")
	public void fetchGasConfig() {
		for (Integer chainId : SUPPORTED_CHAIN_IDS) {
			updateConfigParam(chainId);
		}
	}

	private void initContext(Integer chainId) {
		MDC.clear();

		String requestId = RequestIdGenerator.generate();
		MDC.put(Constants.REQUEST_ID, requestId);
		MDC.put(Constants.CHAIN_ID, String.valueOf(chainId));

		ExecutionContextUtil context = ExecutionContextUtil.getContext();
		context.setRequestId(requestId);
		context.setChainId(chainId);
		context.setMainNet(Objects.equals(chainId, Constants.MAIN_NET_CHAIN_ID));
	}

	private void updateConfigParam(Integer chainId) {
		initContext(chainId);
		for (Integer configId : CONFIG_PARAMS) {
			try {
				TonConfigParamDto configParamDto = tonCoreServiceHelper.getConfigParam(configId);
				setTonConfig(configId, configParamDto, chainId);
			} catch (Exception e) {
				LOG.warn("Failed to fetch or process config param {} for chain {}: {}", configId, chainId, e.getMessage(), e);
			}
		}
	}

	private void setTonConfig(Integer configId, TonConfigParamDto configParamDto, Integer chainId) {
		ConfigParam config = getConfigByChainId(chainId);
		String bytes = configParamDto.getResult().getConfig().getBytes();
		if (StringUtil.nullOrEmpty(bytes)) {
			LOG.warn("Config param bytes are empty for config ID: {} and chain ID: {}", configId, chainId);
			return;
		}
		Cell cell = CellBuilder.beginCell().fromBocBase64(bytes).endCell();
		CellSlice slice = CellSlice.beginParse(cell);
		switch (configId) {
			case 21 -> {
				slice.loadUint(144); // Skip unused bits
				config.setGasPrice(slice.loadUint(64).longValue());
			}
			case 25 -> {
				slice.loadUint(8); // Skip unused bits
				config.setLumpPrice(slice.loadUint(64).longValue());
				config.setBitPrice(slice.loadUint(64).longValue());
				config.setCellPrice(slice.loadUint(64).longValue());
			}
			default -> LOG.warn("Unsupported config param ID: {}", configId);
		}
	}
}

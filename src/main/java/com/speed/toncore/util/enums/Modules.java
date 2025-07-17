package com.speed.toncore.util.enums;

import com.speed.toncore.domain.model.TonAddress;
import com.speed.toncore.domain.model.TonFeeAccount;
import com.speed.toncore.domain.model.TonListener;
import com.speed.toncore.domain.model.TonMainAccount;
import com.speed.toncore.domain.model.TonOnChainTx;
import com.speed.toncore.domain.model.TonSweepTx;
import com.speed.toncore.domain.model.TonToken;
import com.speed.toncore.domain.model.TonUsedAddress;
import com.speed.toncore.domain.model.WithdrawProcess;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum Modules {
	TON_ADDRESS("acc", TonAddress.class.getSimpleName(), "ton_address"),
	TON_USED_ADDRESS("uacc", TonUsedAddress.class.getSimpleName(), "ton_used_address"),
	TON_MAIN_ACCOUNTS("macc", TonMainAccount.class.getSimpleName(), "ton_main_accounts"),
	TON_FEE_ACCOUNTS("facc", TonFeeAccount.class.getSimpleName(), "ton_fee_accounts"),
	TON_TOKENS("tkn", TonToken.class.getSimpleName(), "ton_tokens"),
	TON_LISTENERS("lis", TonListener.class.getSimpleName(), "ton_listeners"),
	ON_CHAIN_TX("tx", TonOnChainTx.class.getSimpleName(), "ton_on_chain_tx"),
	TON_SWEEP_TX("stx", TonSweepTx.class.getSimpleName(), "ton_sweep_tx"),
	TON_WITHDRAW_PROCESS("twp", WithdrawProcess.class.getSimpleName(), "ton_withdraw_process");
	private static final Map<String, String> modulesMap = Arrays.stream(Modules.values())
			.collect(Collectors.toMap(mod -> mod.objectType, Modules::getPrefix));
	private static final Map<String, String> objectsMap = Arrays.stream(Modules.values())
			.collect(Collectors.toMap(mod -> mod.objectType, Modules::getObject));
	private final String prefix;
	private final String objectType;
	private final String object;

	Modules(String prefix, String objectType, String object) {
		this.prefix = prefix;
		this.objectType = objectType;
		this.object = object;
	}

	public static String getPrefixByModule(String module) {
		return modulesMap.get(module);
	}

	public static String getObjectByModule(String module) {
		return objectsMap.get(module);
	}

	private String getPrefix() {
		return prefix;
	}

	private String getObject() {
		return object;
	}
}

package com.speed.toncore.util;

import com.speed.javacommon.exceptions.InternalServerErrorException;
import com.speed.javacommon.util.DateTimeUtil;
import com.speed.javacommon.util.StringUtil;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.interceptor.ExecutionContextUtil;
import lombok.experimental.UtilityClass;
import org.ton.ton4j.address.Address;
import org.ton.ton4j.cell.Cell;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class TonUtils {

	public String toRawAddress(String address) {
		if (StringUtil.nullOrEmpty(address)) {
			return null;
		}
		try {
			return Address.of(address).toRaw().toUpperCase();
		} catch (Exception e) {
			throw new InternalServerErrorException(String.format(Errors.INVALID_ADDRESS, address), e);
		}
	}

	public String rawToUserFriendlyAddress(String rawAddress) {
		if (StringUtil.nullOrEmpty(rawAddress)) {
			return null;
		}
		try {
			if (Objects.equals(ExecutionContextUtil.getContext().getChainId(), Constants.MAIN_NET_CHAIN_ID)) {
				return Address.of(rawAddress).toBounceable();
			} else {
				return Address.of(rawAddress).toBounceableTestnet();
			}
		} catch (Exception e) {
			throw new InternalServerErrorException(String.format(Errors.INVALID_ADDRESS, rawAddress), e);
		}
	}

	public String generateTransferTransactionReference() {
		return String.format("transfer_%s", generateUniqueReference());
	}

	public String generateSweepTransactionReference() {
		return String.format("sweep_%s", generateUniqueReference());
	}

	/**
	 * Generates a unique reference using current UTC time in milliseconds and a thread-safe random long.
	 * - `ThreadLocalRandom` is used for efficient, low-contention random generation in multithreaded environments.
	 * - `Long.toHexString(...)` is used to convert the random long to a compact hexadecimal string.
	 * This approach is fast and lightweight (non-cryptographic), suitable for internal unique references without
	 * the overhead of cryptographic algorithms like UUID or SecureRandom.
	 */
	private String generateUniqueReference() {
		return DateTimeUtil.currentEpochMilliSecondsUTC() + "_" + Long.toHexString(ThreadLocalRandom.current().nextLong());
	}

	/**
	 * Deserializes a hex-encoded transaction reference string from a base64-encoded BOC message.
	 *
	 * @param message Base64-encoded BOC message
	 * @return Decoded string transaction reference
	 */
	public String deserializeTransactionReference(String message) {
		if (StringUtil.nullOrEmpty(message)) {
			return "";
		}
		return new String(HexFormat.of().parseHex(Cell.fromBocBase64(message).toString().substring(8)), StandardCharsets.UTF_8);
	}
}

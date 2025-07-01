package com.speed.toncore.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.speed.javacommon.exceptions.BadRequestException;
import com.speed.toncore.constants.Errors;
import lombok.extern.slf4j.Slf4j;
import org.ton.ton4j.address.Address;

import java.io.IOException;

@Slf4j
public class TonRawAddressDeserializer extends JsonDeserializer<String> {

	@Override
	public String deserialize(JsonParser p, DeserializationContext context) throws IOException {
		String address = p.getText();
		if (address == null || address.isEmpty()) {
			return null;
		}
		try {
			return Address.of(address).toRaw().toUpperCase();
		} catch (Exception e) {
			LOG.error("Failed to deserialize address: {}", address, e);
			throw new BadRequestException(String.format(Errors.INVALID_ADDRESS, address), null, null);
		}
	}
}

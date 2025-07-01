package com.speed.toncore.util;

import jakarta.validation.constraints.NotNull;
import org.springframework.cache.interceptor.KeyGenerator;

import java.lang.reflect.Method;
import java.util.Arrays;

public class CachingKeyGenerator implements KeyGenerator {

	public static String generateCacheKey(String... params) {
		return String.join("-", params);
	}

	@NotNull
	@Override
	public Object generate(@NotNull Object target, @NotNull Method method, @NotNull Object... params) {
		String[] stringParams = Arrays.stream(params).map(Object::toString).toArray(String[]::new);
		return generateCacheKey(stringParams);
	}
}

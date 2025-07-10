package com.speed.toncore.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.util.CachingKeyGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class CacheConfig {

	private CaffeineCache buildDefaultCache(String name) {
		return new CaffeineCache(name, Caffeine.newBuilder().build());
	}

	@Bean(Constants.CACHE_KEY_GENERATOR)
	public KeyGenerator keyGenerator() {
		return new CachingKeyGenerator();
	}

	@Bean
	public CacheManager cacheManager() {
		CaffeineCache receiveAddressesCache = buildDefaultCache(Constants.CacheNames.RECEIVE_ADDRESSES);
		CaffeineCache sendAddressesCache = buildDefaultCache(Constants.CacheNames.SEND_ADDRESSES);
		CaffeineCache jettonResponseCache = buildDefaultCache(Constants.CacheNames.JETTON_RESPONSE);

		SimpleCacheManager cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(Arrays.asList(receiveAddressesCache, sendAddressesCache, jettonResponseCache));
		return cacheManager;
	}
}


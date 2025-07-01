package com.speed.toncore.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@Getter
@ConfigurationProperties(prefix = "async")
@AllArgsConstructor(onConstructor_ = @ConstructorBinding)
public class AsyncConfig {

	private final int poolSize;
	private final int maxPoolSize;
	private final int maxQueueCapacity;
}
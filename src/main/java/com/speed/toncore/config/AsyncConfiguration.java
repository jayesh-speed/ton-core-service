package com.speed.toncore.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AsyncConfiguration implements AsyncConfigurer {

	private final AsyncConfig asyncConfig;

	@Bean(destroyMethod = "shutdown")
	public Executor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(asyncConfig.getPoolSize());
		executor.setMaxPoolSize(asyncConfig.getMaxPoolSize());
		executor.setQueueCapacity(asyncConfig.getMaxQueueCapacity());
		executor.setTaskDecorator(new AsyncContextResolver());
		executor.setThreadNamePrefix("ASYNC-THREAD");
		executor.initialize();
		return executor;
	}
}

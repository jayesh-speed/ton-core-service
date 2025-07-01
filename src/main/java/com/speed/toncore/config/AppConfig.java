package com.speed.toncore.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@Getter
@ConfigurationProperties(prefix = "app")
@AllArgsConstructor(onConstructor_ = @ConstructorBinding)
public class AppConfig {

	private final String schedulerEventValue;
	private final boolean useMainnet;
}

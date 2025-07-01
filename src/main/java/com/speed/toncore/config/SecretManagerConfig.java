package com.speed.toncore.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@Getter
@AllArgsConstructor(onConstructor_ = @ConstructorBinding)
@ConfigurationProperties(prefix = "aws.secretmanager")
public class SecretManagerConfig {

    private final String region;
    private final String mainnetSecretId;
    private final String testnetSecretId;
}

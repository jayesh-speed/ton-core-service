package com.speed.toncore;

import com.speed.javacommon.repository.SpeedBaseRepositoryImpl;
import com.speed.toncore.ton.Ton4jClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
@ComponentScan(basePackages = "com.speed")
@ConfigurationPropertiesScan
@EntityScan
@EnableScheduling
@EnableCaching
@EnableRetry
@EnableJpaRepositories(repositoryBaseClass = SpeedBaseRepositoryImpl.class)
public class TonCoreServiceApplication implements CommandLineRunner {

	private final Ton4jClient ton4jClient;

	@Autowired
	public TonCoreServiceApplication(Ton4jClient ton4jClient) {
		this.ton4jClient = ton4jClient;
	}

	public static void main(String[] args) {
		SpringApplication.run(TonCoreServiceApplication.class, args);
	}

	@Override
	public void run(String... args) {
		ton4jClient.setupTonNodes();
	}
}

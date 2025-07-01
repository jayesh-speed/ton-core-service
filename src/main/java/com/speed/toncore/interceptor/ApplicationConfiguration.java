package com.speed.toncore.interceptor;

import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.web.client.RestClient;

import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfiguration {

	@Bean
	@Primary
	public RestClient restClient(RestClient.Builder restClientBuilder) {
		return restClientBuilder.build();
	}

	@Bean
	@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
	public FilterRegistrationBean<ApiInterceptor> jwtFilter() {

		FilterRegistrationBean<ApiInterceptor> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new ApiInterceptor());
		return registrationBean;
	}

	@Bean
	public OkHttpClient okHttpClient() {
		return new OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).build();
	}
}

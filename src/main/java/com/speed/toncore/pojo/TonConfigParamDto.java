package com.speed.toncore.pojo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TonConfigParamDto {

	private Result result;

	@Getter
	@Setter
	public static class Result {

		private Config config;
	}

	@Getter
	@Setter
	public static class Config {

		private String bytes;
	}
}
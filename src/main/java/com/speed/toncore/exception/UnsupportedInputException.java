package com.speed.toncore.exception;

import lombok.Getter;

@Getter
public class UnsupportedInputException extends RuntimeException {

	private static final long serialVersionUID = -2082769954330323601L;

	public UnsupportedInputException(String message) {
		super(message);
	}

	public UnsupportedInputException(Throwable ex) {
		super(ex);
	}
}

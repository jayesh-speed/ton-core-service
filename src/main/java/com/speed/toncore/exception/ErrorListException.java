package com.speed.toncore.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.HashSet;
import java.util.Set;

@Getter
public class ErrorListException extends RuntimeException {

	private static final long serialVersionUID = 8151581614493053014L;

	private final Set<String> errors;
	private final HttpStatus httpStatus;
	private final String errorType;

	public ErrorListException(HttpStatus httpStatus) {
		this(httpStatus, null, HashSet.newHashSet(1));
	}

	public ErrorListException(HttpStatus httpStatus, String errorType) {
		this(httpStatus, errorType, HashSet.newHashSet(1));
	}

	public ErrorListException(HttpStatus httpStatus, Set<String> errors) {
		this(httpStatus, null, errors);
	}

	public ErrorListException(HttpStatus httpStatus, String errorType, Set<String> errors) {
		this.httpStatus = httpStatus;
		this.errorType = errorType;
		this.errors = errors;
	}

	public void addError(String error) {
		errors.add(error);
	}
}

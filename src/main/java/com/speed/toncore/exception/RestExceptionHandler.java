package com.speed.toncore.exception;

import com.speed.javacommon.constants.CommonConstants;
import com.speed.javacommon.enums.CommonLogActions;
import com.speed.javacommon.exceptions.BadRequestException;
import com.speed.javacommon.exceptions.EntityNotFoundException;
import com.speed.javacommon.exceptions.InternalServerErrorException;
import com.speed.javacommon.log.LogHolder;
import com.speed.toncore.constants.Constants;
import com.speed.toncore.constants.Errors;
import com.speed.toncore.interceptor.ApiError;
import jakarta.servlet.ServletException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Set;
import java.util.stream.Collectors;

import static net.logstash.logback.marker.Markers.appendEntries;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(@NotNull HttpMessageNotReadableException ex, @NotNull HttpHeaders headers,
			@NotNull HttpStatusCode status, @NotNull WebRequest request) {
		LOG.error(ex.getMessage(), ex);
		ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "INVALID_JSON");
		return buildResponseEntity(apiError);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, @NotNull HttpHeaders headers,
			@NotNull HttpStatusCode status, @NotNull WebRequest request) {
		Set<String> errors = ex.getBindingResult().getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.toSet());
		ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, errors);
		return buildResponseEntity(apiError);
	}

	@Override
	protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex, @NotNull HttpHeaders headers,
			@NotNull HttpStatusCode status, @NotNull WebRequest request) {
		ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());
		return buildResponseEntity(apiError);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, @NotNull HttpHeaders headers,
			@NotNull HttpStatusCode status, @NotNull WebRequest request) {
		String error = String.format(Errors.UNSUPPORTED_MEDIA_TYPE, ex.getContentType(), MediaType.toString(ex.getSupportedMediaTypes()));
		ApiError apiError = new ApiError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, error);
		return buildResponseEntity(apiError);
	}

	@Override
	protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, @NotNull HttpHeaders headers,
			@NotNull HttpStatusCode status, @NotNull WebRequest request) {
		String error = String.format(Errors.PARAMETER_MISSING, ex.getParameterName());
		ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, error);
		return buildResponseEntity(apiError);
	}

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<Object> handleBadRequestException(BadRequestException ex) {
		ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getErrorType());
		return buildResponseEntity(apiError);
	}

	@ExceptionHandler(ErrorListException.class)
	public ResponseEntity<Object> handleErrorList(ErrorListException ex) {
		ApiError apiError = new ApiError(ex.getHttpStatus(), ex.getErrors(), ex.getErrorType());
		return buildResponseEntity(apiError);
	}

	@ExceptionHandler(ServletException.class)
	public ResponseEntity<Object> handleServletException(ServletException ex) {
		ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());
		return buildResponseEntity(apiError);
	}

	@ExceptionHandler(InternalServerErrorException.class)
	public ResponseEntity<Object> handleInternalServerError(InternalServerErrorException ex) {
		LOG.error(ex.getMessage(), ex.getCause());
		ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR,
				String.format(Errors.INTERNAL_SERVER_ERROR, MDC.get(CommonConstants.REQUEST_ID)));
		return buildResponseEntity(apiError);
	}

	@ExceptionHandler(UnsupportedInputException.class)
	public ResponseEntity<Object> handleUnsupportedInput(UnsupportedInputException ex) {
		ApiError apiError = new ApiError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage());
		return buildResponseEntity(apiError);
	}

	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException ex) {
		ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, ex.getMessage(), ex.getErrorType());
		return buildResponseEntity(apiError);
	}

	private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
		LogHolder logHolder = new LogHolder();
		logHolder.setAction(CommonLogActions.API_EXCEPTION);
		logHolder.put(Constants.API_ERROR, apiError);
		LOG.error(appendEntries(logHolder.getAttributes()), null);
		return new ResponseEntity<>(apiError, apiError.getHttpStatus());
	}
}

package com.example.assistant.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public BusinessException(String errorMessage) {
		super(errorMessage);
	}

	public BusinessException(String errorMessage, Throwable cause) {
		super(errorMessage, cause);
	}
}

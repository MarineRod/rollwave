package com.rollwave.exception;

public class InvalidContentException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InvalidContentException(String message) {
		super(message);
	}
}

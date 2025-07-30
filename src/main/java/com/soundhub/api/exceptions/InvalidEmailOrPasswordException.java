package com.soundhub.api.exceptions;

public class InvalidEmailOrPasswordException extends RuntimeException {
	public InvalidEmailOrPasswordException(String message) {
		super(message);
	}
}

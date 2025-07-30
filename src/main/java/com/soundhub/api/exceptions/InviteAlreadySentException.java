package com.soundhub.api.exceptions;

public class InviteAlreadySentException extends RuntimeException {
	public InviteAlreadySentException(String message) {
		super(message);
	}
}

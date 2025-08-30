package com.soundhub.api.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KafkaResponseException extends Throwable {
	private final String errorType;
	private final int statusCode;
	private final String detail;
}

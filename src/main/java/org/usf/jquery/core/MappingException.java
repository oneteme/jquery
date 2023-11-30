package org.usf.jquery.core;

@SuppressWarnings("serial")
public final class MappingException extends RuntimeException {

	public MappingException(Throwable cause) {
		super(cause);
	}

	public MappingException(String message, Throwable cause) {
		super(message, cause);
	}
}

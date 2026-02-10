package org.usf.jquery.web.proxy;

@SuppressWarnings("serial")
public final class ResourceInvocationException extends RuntimeException {

	public ResourceInvocationException(Throwable cause) {
		super(cause);
	}

	public ResourceInvocationException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ResourceInvocationException(String message) {
		super(message);
	}
}

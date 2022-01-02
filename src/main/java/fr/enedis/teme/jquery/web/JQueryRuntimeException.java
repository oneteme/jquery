package fr.enedis.teme.jquery.web;

@SuppressWarnings("serial")
public class JQueryRuntimeException extends RuntimeException {

	public JQueryRuntimeException(String message) {
		super(message);
	}

	public JQueryRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
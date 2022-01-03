package fr.enedis.teme.jquery.web;

import lombok.NonNull;

@SuppressWarnings("serial")
public final class ParameterInvalidValueException extends JQueryRuntimeException {

	public ParameterInvalidValueException(String message) {
		super(message);
	}

	public ParameterInvalidValueException(String message, Throwable cause) {
		super(message, cause);
	}	

	public static ParameterInvalidValueException invalidParameterValueException(@NonNull String value) {
		return new ParameterInvalidValueException("invalid value : '" + value + "'");
	}

	public static ParameterInvalidValueException invalidParameterValueException(@NonNull String value, Throwable cause) {
		return new ParameterInvalidValueException("invalid value : '" + value + "'", cause);
	}

}

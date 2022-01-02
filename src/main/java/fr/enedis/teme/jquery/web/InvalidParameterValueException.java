package fr.enedis.teme.jquery.web;

@SuppressWarnings("serial")
public final class InvalidParameterValueException extends JQueryRuntimeException {

	public InvalidParameterValueException(String message) {
		super(message);
	}

	public InvalidParameterValueException(String message, Throwable cause) {
		super(message, cause);
	}	

	public static InvalidParameterValueException invalidParameterValueException(String value) {
		return new InvalidParameterValueException("invalid value : '" + value + "'");
	}

	public static InvalidParameterValueException invalidParameterValueException(String value, Throwable cause) {
		return new InvalidParameterValueException("invalid value : '" + value + "'", cause);
	}

}

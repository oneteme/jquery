package fr.enedis.teme.jquery.web;

@SuppressWarnings("serial")
public final class InvalidParameterValueException extends JQueryRuntimeException {

	public InvalidParameterValueException(String message) {
		super(message);
	}

	public static InvalidParameterValueException invalidParameterValueException(String value) {
		return new InvalidParameterValueException("invalid value : '" + value + "'");
	}
	
}

package fr.enedis.teme.jquery.web;

@SuppressWarnings("serial")
public final class ParameterRequiredException extends JQueryRuntimeException {

	public ParameterRequiredException(String message) {
		super(message);
	}
	
	public static ParameterRequiredException missingParameterException(String name) {
		return new ParameterRequiredException("require parameter '" + name + "'");
	}

}

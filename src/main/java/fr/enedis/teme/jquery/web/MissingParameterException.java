package fr.enedis.teme.jquery.web;

@SuppressWarnings("serial")
public final class MissingParameterException extends JQueryRuntimeException {

	public MissingParameterException(String message) {
		super(message);
	}
	
	public static MissingParameterException missingParameterException(String name) {
		return new MissingParameterException("'" + name + "' parameter is missing");
	}

}

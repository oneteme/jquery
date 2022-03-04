package fr.enedis.teme.jquery.web;

import lombok.NonNull;

@SuppressWarnings("serial")
public final class ParameterRequiredException extends JQueryRuntimeException {

	public ParameterRequiredException(String message) {
		super(message);
	}
	
	public static ParameterRequiredException missingParameterException(@NonNull String name) {
		return new ParameterRequiredException("require '" + name +"' parameter");
	}

}

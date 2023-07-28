package org.usf.jquery.web;

/**
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public final class MissingParameterException extends IllegalArgumentException {

	public MissingParameterException(String s) {
		super(s);
	}
	
	public static MissingParameterException missingParameterException(String parameter) {
		return new MissingParameterException("require " + parameter + " parameter");
	}

}

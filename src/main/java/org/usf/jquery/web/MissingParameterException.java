package org.usf.jquery.web;

import static org.usf.jquery.core.SqlStringBuilder.quote;

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
		return new MissingParameterException("require " + quote(parameter) + " parameter");
	}

}

package org.usf.jquery.web;

import static java.util.stream.Collectors.joining;

import java.util.stream.Stream;

import org.usf.jquery.core.SqlStringBuilder;

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
	
	public static MissingParameterException missingParameterException(String... parameters) {
		return new MissingParameterException("require " +  
				Stream.of(parameters).map(SqlStringBuilder::quote).collect(joining(" or ")) + " parameter");
	}

}

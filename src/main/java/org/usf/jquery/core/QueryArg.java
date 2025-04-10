package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
public record QueryArg(Object value, int type) {
	
	public static QueryArg arg(Object value, int type) {
		return new QueryArg(value, type);
	}
}

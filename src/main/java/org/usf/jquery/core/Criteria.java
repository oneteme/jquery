package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
public interface Criteria extends Column, Chainable<Criteria> {
	
	@Override
	default JDBCType getType() {
		return JDBCType.BOOLEAN;
	}
}
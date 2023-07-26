package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.space;

/**
 * 
 * @author u$f
 *
 */
public enum LogicalOperator {
	
	AND, OR;
	
	public String sql() {
		return space(name());
	}
}
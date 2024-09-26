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
	
	public <T extends Chainable<T>> T combine(T o1, T o2) {
		return o1.append(this, o2);
	}
}
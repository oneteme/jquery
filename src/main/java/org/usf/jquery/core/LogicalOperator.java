package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.space;

enum LogicalOperator {
	
	AND, OR;
	
	public String sql() {
		return space(name());
	}
}
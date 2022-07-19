package org.usf.jquery;

import static org.usf.jquery.SqlStringBuilder.space;

enum LogicalOperator {
	
	AND, OR;
	
	public String sql() {
		return space(this.name());
	}
}
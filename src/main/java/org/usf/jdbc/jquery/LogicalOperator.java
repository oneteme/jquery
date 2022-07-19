package org.usf.jdbc.jquery;

import static org.usf.jdbc.jquery.SqlStringBuilder.space;

enum LogicalOperator {
	
	AND, OR;
	
	public String sql() {
		return space(this.name());
	}
}
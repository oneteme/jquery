package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.SqlStringBuilder.space;

enum LogicalOperator {
	
	AND, OR;
	
	public String sql() {
		return space(this.name());
	}
}
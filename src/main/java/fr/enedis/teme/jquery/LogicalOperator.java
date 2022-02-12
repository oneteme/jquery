package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.SqlStringBuilder.SPACE_SEPARATOR;

public enum LogicalOperator {
	
	AND, OR;
	
	public String sql() {
		return SPACE_SEPARATOR + this.name() + SPACE_SEPARATOR;
	}
}
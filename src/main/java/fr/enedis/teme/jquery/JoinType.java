package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.SqlStringBuilder.SPACE_SEPARATOR;

enum JoinType {	

	INNER, LEFT, RIGHT;

	public String sql() {
		return this.name() + SPACE_SEPARATOR;
	}
}

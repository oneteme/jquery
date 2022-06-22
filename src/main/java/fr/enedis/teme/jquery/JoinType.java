package fr.enedis.teme.jquery;

enum JoinType {	

	INNER, LEFT, RIGHT, FULL;

	public String sql() {
		return this.name();
	}
}

package fr.enedis.teme.jquery;

enum JoinType {	

	INNER, LEFT, RIGHT;

	public String sql() {
		return this.name() + " ";
	}
}

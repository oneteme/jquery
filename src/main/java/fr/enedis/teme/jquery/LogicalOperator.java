package fr.enedis.teme.jquery;

public enum LogicalOperator {
	
	AND, OR;
	
	public String sql() {
		return " " + this.name() + " ";
	}
}
package fr.enedis.teme.jquery;

public enum LogicalOperator {
	
	AND, OR;
	
	@Override
	public String toString() {
		return " " + this.name() + " ";
	}
}
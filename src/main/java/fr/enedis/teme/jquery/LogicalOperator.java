package fr.enedis.teme.jquery;

enum LogicalOperator {
	
	AND, OR;
	
	@Override
	public String toString() {
		return " " + this.name() + " ";
	}
}
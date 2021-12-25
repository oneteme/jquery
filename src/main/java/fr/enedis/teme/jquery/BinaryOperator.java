package fr.enedis.teme.jquery;

enum BinaryOperator {
	
	AND, OR;
	
	@Override
	public String toString() {
		return " " + this.name() + " ";
	}
}
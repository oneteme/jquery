package fr.enedis.teme.jquery;

import static java.util.stream.Collectors.joining;

import java.util.stream.Collector;

public enum LogicalOperator {
	
	AND, OR;
	
	public String join(String[] values) {
		return String.join(sql(), values);
	}
	
	public Collector<CharSequence, ?, String> joiner(){
		return joining(sql());
	}

	public String sql() {
		return " " + this.name() + " ";
	}
	
	@Override
	public String toString() {
		return " " + this.name() + " ";
	}
}
package fr.enedis.teme.jquery;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ArithmeticOperator {
	
	ADD("+"), SUB("-"), MULT("*"), DIV("/");
	
	private final String symbol;
	
	public String sql() {
		return symbol;
	}

}

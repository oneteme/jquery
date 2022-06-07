package fr.enedis.teme.jquery;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ArithmeticOperator {
	
	ADD("+"), SUB("-"), MULT("*"), DIV("/"), MOD("%"), POW("^");
	
	private final String symbol;
	
	public String sql() {
		return symbol;
	}

}

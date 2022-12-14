package org.usf.jquery.core;

import static org.usf.jquery.core.Utils.hasSize;
import static org.usf.jquery.core.Validation.illegalArgumentIf;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
enum ArithmeticOperator implements DBOperator {
	
	ADD("+"), SUB("-"), MULT("*"), DIV("/"), MOD("%"), POW("^");
	
	@Override
	public String sql(QueryParameterBuilder ph, Object operand, Object... args) {
		illegalArgumentIf(operand == null || !hasSize(args, 1),  ()-> this.name() + " require 2 parameters");
		return ph.appendNumber(operand) + this.symbol + ph.appendNumber(args[0]);
	}
	
	final String symbol;
}

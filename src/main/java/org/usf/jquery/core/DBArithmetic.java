package org.usf.jquery.core;

import static org.usf.jquery.core.Utils.hasSize;
import static org.usf.jquery.core.Validation.illegalArgumentIf;

@FunctionalInterface
interface DBArithmetic extends DBOperation {
	
	String symbol();
	
	@Override
	default String sql(QueryParameterBuilder ph, Object[] args) {
		illegalArgumentIf(!hasSize(args, 2), ()-> symbol() + " operation takes two parameters");
		return ph.appendNumber(args[0]) + symbol() + ph.appendNumber(args[1]);
	}
	
	default OperationColumn args(Object left, Object right) {
		return new OperationColumn(this, new Object[] {left, right});
	}

	static DBArithmetic plus() {
		return operator("+");
	}

	static DBArithmetic minus() {
		return operator("-");
	}

	static DBArithmetic multiply() {
		return operator("*");
	}
	
	static DBArithmetic divise() {
		return operator("/");
	}
	
	static DBArithmetic mode() {
		return operator("%");
	}
	
	static DBArithmetic pow() {
		return operator("^");
	}
	
	static DBArithmetic operator(final String symbol) {
		return ()-> symbol;
	}
}

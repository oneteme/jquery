package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
interface DBArithmetic extends DBOperation {
	
	String symbol();
	
	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		requireNArgs(2, args, ()-> "operation " + symbol());
		return builder.appendNumber(args[0]) + symbol() + builder.appendNumber(args[1]);
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

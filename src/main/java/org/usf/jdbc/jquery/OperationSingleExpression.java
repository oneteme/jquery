package org.usf.jdbc.jquery;

import static org.usf.jdbc.jquery.ArithmeticOperator.ADD;
import static org.usf.jdbc.jquery.ArithmeticOperator.DIV;
import static org.usf.jdbc.jquery.ArithmeticOperator.MOD;
import static org.usf.jdbc.jquery.ArithmeticOperator.MULT;
import static org.usf.jdbc.jquery.ArithmeticOperator.POW;
import static org.usf.jdbc.jquery.ArithmeticOperator.SUB;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter(value = AccessLevel.PACKAGE)
@RequiredArgsConstructor
final class OperationSingleExpression implements DBExpression {
	
	private final DBOperator operator;
	private final Object value; //nullable

	@Override
	public String sql(QueryParameterBuilder builder, Object operand) {
		return DBCallable.sql(operator, builder, operand, value);
	}

	public static final OperationSingleExpression plus(Object value) {
		return new OperationSingleExpression(ADD, value);
	}

	public static final OperationSingleExpression minus(Object value) {
		return new OperationSingleExpression(SUB, value);
	}

	public static final OperationSingleExpression multiply(Object value) {
		return new OperationSingleExpression(MULT, value);
	}
	
	public static final OperationSingleExpression divise(Object value) {
		return new OperationSingleExpression(DIV, value);
	}
	
	public static final OperationSingleExpression mode(Object value) {
		return new OperationSingleExpression(MOD, value);
	}
	
	public static final OperationSingleExpression pow(Object value) {
		return new OperationSingleExpression(POW, value);
	}
	
}

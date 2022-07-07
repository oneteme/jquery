package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.ArithmeticOperator.ADD;
import static fr.enedis.teme.jquery.ArithmeticOperator.DIV;
import static fr.enedis.teme.jquery.ArithmeticOperator.MOD;
import static fr.enedis.teme.jquery.ArithmeticOperator.MULT;
import static fr.enedis.teme.jquery.ArithmeticOperator.POW;
import static fr.enedis.teme.jquery.ArithmeticOperator.SUB;

import lombok.RequiredArgsConstructor;

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

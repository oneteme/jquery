package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.CompareOperator.EQ;
import static fr.enedis.teme.jquery.CompareOperator.GE;
import static fr.enedis.teme.jquery.CompareOperator.GT;
import static fr.enedis.teme.jquery.CompareOperator.IN;
import static fr.enedis.teme.jquery.CompareOperator.IS_NOT_NULL;
import static fr.enedis.teme.jquery.CompareOperator.IS_NULL;
import static fr.enedis.teme.jquery.CompareOperator.LE;
import static fr.enedis.teme.jquery.CompareOperator.LIKE;
import static fr.enedis.teme.jquery.CompareOperator.LT;
import static fr.enedis.teme.jquery.CompareOperator.NE;
import static fr.enedis.teme.jquery.CompareOperator.NOT_IN;
import static fr.enedis.teme.jquery.CompareOperator.NOT_LIKE;
import static fr.enedis.teme.jquery.ParameterHolder.addWithValue;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class OperatorSingleExpression<T> implements OperatorExpression {

	@NonNull
	private final CompareOperator operator;
	private final T value;//nullable
	
	@Override
	public String sql(String cn, ParameterHolder arg) {
		return cn + operator.sql(value, arg);
	}
	
	@Override
	public OperatorExpression append(LogicalOperator op, OperatorExpression exp) {
		return new OperatorExpressionGroup(op, this, exp);
	}

	public static final <T> OperatorSingleExpression<T> equal(T value) {
		return new OperatorSingleExpression<>(EQ, value);
	}

	public static final <T> OperatorSingleExpression<T> notEqual(T value) {
		return new OperatorSingleExpression<>(NE, value);
	}
	
	public static final <T> OperatorSingleExpression<T> lessThan(@NonNull T value) {
		return new OperatorSingleExpression<>(LT, value);
	}

	public static final <T> OperatorSingleExpression<T> lessOrEqual(@NonNull T value) {
		return new OperatorSingleExpression<>(LE, value);
	}

	public static final <T> OperatorSingleExpression<T> greaterThan(@NonNull T value) {
		return new OperatorSingleExpression<>(GT, value);
	}

	public static final <T> OperatorSingleExpression<T> greaterOrEqual(@NonNull T value) {
		return new OperatorSingleExpression<>(GE, value);
	}

	public static final OperatorSingleExpression<String> like(@NonNull String value) {
		return new OperatorSingleExpression<>(LIKE, value);
	}

	@SafeVarargs
	public static final <T> OperatorSingleExpression<T[]> in(@NonNull T... values) {
		return new OperatorSingleExpression<>(IN, values);
	}

	@SafeVarargs
	public static final <T> OperatorSingleExpression<T[]> notIn(@NonNull T... values) {
		return new OperatorSingleExpression<>(NOT_IN, values);
	}

	public static final OperatorSingleExpression<String> notLike(@NonNull String value) {
		return new OperatorSingleExpression<>(NOT_LIKE, value);
	}
	
	public static final OperatorSingleExpression<Void> isNull() {
		return  new OperatorSingleExpression<>(IS_NULL, null);
	}

	public static final OperatorSingleExpression<Void> isNotNull() {
		return  new OperatorSingleExpression<>(IS_NOT_NULL, null);
	}

	@Override
	public String toString() {
		return sql("", addWithValue());
	}
}

package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Operator.EQ;
import static fr.enedis.teme.jquery.Operator.GE;
import static fr.enedis.teme.jquery.Operator.GT;
import static fr.enedis.teme.jquery.Operator.IN;
import static fr.enedis.teme.jquery.Operator.IS_NOT_NULL;
import static fr.enedis.teme.jquery.Operator.IS_NULL;
import static fr.enedis.teme.jquery.Operator.LE;
import static fr.enedis.teme.jquery.Operator.LIKE;
import static fr.enedis.teme.jquery.Operator.LT;
import static fr.enedis.teme.jquery.Operator.NE;
import static fr.enedis.teme.jquery.Operator.NOT_IN;
import static fr.enedis.teme.jquery.Operator.NOT_LIKE;
import static fr.enedis.teme.jquery.ParameterHolder.addWithValue;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class OperatorExpression<T> implements DBExpression {

	@NonNull
	private final Operator operator;
	private final T value;//nullable
	
	@Override
	public String sql(String cn, ParameterHolder arg) {
		return cn + operator.sql(value, arg);
	}

	@Override
	public String toString() {
		return sql("", addWithValue());
	}

	public static final <T> OperatorExpression<T> equal(T value) {
		return new OperatorExpression<>(EQ, value);
	}

	public static final <T> OperatorExpression<T> notEquals(T value) {
		return new OperatorExpression<>(NE, value);
	}
	
	public static final <T> OperatorExpression<T> lessThan(@NonNull T value) {
		return new OperatorExpression<>(LT, value);
	}

	public static final <T> OperatorExpression<T> lessOrEquals(@NonNull T value) {
		return new OperatorExpression<>(LE, value);
	}

	public static final <T> OperatorExpression<T> greaterThan(@NonNull T value) {
		return new OperatorExpression<>(GT, value);
	}

	public static final <T> OperatorExpression<T> greaterOrEquals(@NonNull T value) {
		return new OperatorExpression<>(GE, value);
	}

	public static final OperatorExpression<String> like(@NonNull String value) {
		return new OperatorExpression<>(LIKE, value);
	}

	public static final OperatorExpression<String> notLike(@NonNull String value) {
		return new OperatorExpression<>(NOT_LIKE, value);
	}
	
	public static final OperatorExpression<Void> isNull() {
		return  new OperatorExpression<>(IS_NULL, null);
	}

	public static final OperatorExpression<Void> isNotNull() {
		return  new OperatorExpression<>(IS_NOT_NULL, null);
	}

	@SafeVarargs
	public static final <T> OperatorExpression<T[]> in(@NonNull T... values) {
		return new OperatorExpression<>(IN, values);
	}

	@SafeVarargs
	public static final <T> OperatorExpression<T[]> notIn(@NonNull T... values) {
		return new OperatorExpression<>(NOT_IN, values);
	}
	
}

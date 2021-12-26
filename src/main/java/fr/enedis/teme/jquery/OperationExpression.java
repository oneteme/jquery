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
import static fr.enedis.teme.jquery.ParameterHolder.staticSql;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class OperationExpression<T> implements DBExpression {

	@NonNull
	private final Operator operator;
	private final T value;//nullable
	
	@Override
	public String sql(String cn, ParameterHolder arg) {
		return cn + operator.sql(value, arg);
	}

	@Override
	public String toString() {
		return sql("", staticSql());
	}
	
	public static final <T> OperationExpression<T> lessThan(@NonNull T value) {
		return new OperationExpression<>(LT, value);
	}

	public static final <T> OperationExpression<T> lessOrEquals(@NonNull T value) {
		return new OperationExpression<>(LE, value);
	}

	public static final <T> OperationExpression<T> greaterThan(@NonNull T value) {
		return new OperationExpression<>(GT, value);
	}

	public static final <T> OperationExpression<T> greaterOrEquals(@NonNull T value) {
		return new OperationExpression<>(GE, value);
	}

	public static final <T> OperationExpression<T> equal(T value) {
		return new OperationExpression<>(EQ, value);
	}

	public static final <T> OperationExpression<T> notEquals(T value) {
		return new OperationExpression<>(NE, value);
	}

	public static final OperationExpression<String> like(@NonNull String value) {
		return new OperationExpression<>(LIKE, value);
	}

	public static final OperationExpression<String> notLike(@NonNull String value) {
		return new OperationExpression<>(NOT_LIKE, value);
	}
	
	public static final OperationExpression<Void> isNull() {
		return  new OperationExpression<>(IS_NULL, null);
	}

	public static final OperationExpression<Void> isNotNull() {
		return  new OperationExpression<>(IS_NOT_NULL, null);
	}

	@SafeVarargs
	public static final <T> OperationExpression<T[]> in(@NonNull T... value) {
		return new OperationExpression<>(IN, value);
	}

	@SafeVarargs
	public static final <T> OperationExpression<T[]> notIn(@NonNull T... value) {
		return new OperationExpression<>(NOT_IN, value);
	}
	
}

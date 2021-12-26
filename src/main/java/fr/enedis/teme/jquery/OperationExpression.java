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
import static fr.enedis.teme.jquery.Validation.illegalArgumentIf;
import static java.lang.reflect.Array.getLength;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.empty;

import java.lang.reflect.Array;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class OperationExpression<T> implements DBExpression {

	@NonNull
	private final Operator operator;
	private final T value; //nullable
	private final boolean dynamic;
	
	@Override
	public String sql(String cn) {
		return cn + operator.sql(value, dynamic);
	}

	@Override
	public Stream<Object> args() {
		if(dynamic && value != null) {
			return isArray(value) ? stream(value) : Stream.of(value);
		}
		return empty();
	}

	@Override
	public String toString() {
		return sql("");
	}
	
	public static final <T> OperationExpression<T> lessThan(boolean dynamic, @NonNull T value) {
		return new OperationExpression<>(LT, value, dynamic);
	}

	public static final <T> OperationExpression<T> lessOrEquals(boolean dynamic, @NonNull T value) {
		return new OperationExpression<>(LE, value, dynamic);
	}

	public static final <T> OperationExpression<T> greaterThan(boolean dynamic, @NonNull T value) {
		return new OperationExpression<>(GT, value, dynamic);
	}

	public static final <T> OperationExpression<T> greaterOrEquals(boolean dynamic, @NonNull T value) {
		return new OperationExpression<>(GE, value, dynamic);
	}

	public static final <T> OperationExpression<T> equals(boolean dynamic, @NonNull T value) {
		return new OperationExpression<>(EQ, value, dynamic);
	}

	public static final <T> OperationExpression<T> notEquals(boolean dynamic, @NonNull T value) {
		return new OperationExpression<>(NE, value, dynamic);
	}

	public static final OperationExpression<String> like(boolean dynamic, @NonNull String value) {
		return new OperationExpression<>(LIKE, value, dynamic);
	}

	public static final OperationExpression<String> notLike(boolean dynamic, @NonNull String value) {
		return new OperationExpression<>(NOT_LIKE, value, dynamic);
	}
	
	public static final OperationExpression<Void> isNull() {
		return  new OperationExpression<>(IS_NULL, null, false);
	}

	public static final OperationExpression<Void> isNotNull() {
		return  new OperationExpression<>(IS_NOT_NULL, null, false);
	}

	@SafeVarargs
	public static final <T> OperationExpression<T[]> in(boolean dynamic, @NonNull T... value) {
		return new OperationExpression<>(IN, value, dynamic);
	}

	@SafeVarargs
	public static final <T> OperationExpression<T[]> notIn(boolean dynamic, @NonNull T... value) {
		return new OperationExpression<>(NOT_IN, value, dynamic);
	}

	static String join(Object o) {
		return stream(o)
				.map(Number.class.isAssignableFrom(o.getClass().getComponentType()) || o.getClass().getComponentType().isPrimitive()
					? Object::toString
					: SqlStringBuilder::constantString)
				.collect(joining(","));
	}
	
	private static Stream<Object> stream(Object o) {
		var type = requireNonNull(o).getClass();
		illegalArgumentIf(!type.isArray(), ()-> o + " not array"); //collection case  ?
		illegalArgumentIf(getLength(o) == 0, "empty array");
		var ct = type.getComponentType();
		if(ct.isPrimitive()) {
			if(int.class.equals(ct)) {
				return  IntStream.of((int[])o).mapToObj(c-> c);
			}
			else if(long.class.equals(ct)) {
				return  LongStream.of((long[])o).mapToObj(c-> c);
			}
			else if(double.class.equals(ct)) {
				return  DoubleStream.of((double[])o).mapToObj(c-> c);
			}
			else {				
				throw new UnsupportedOperationException();
			}
		}
		return Stream.of((Object[])o);
	}
	
	private static boolean isArray(Object o) {
		return o.getClass().isArray();
	}
	
}

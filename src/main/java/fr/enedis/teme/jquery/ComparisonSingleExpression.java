package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.QueryParameterBuilder.addWithValue;
import static fr.enedis.teme.jquery.SqlStringBuilder.EMPTY_STRING;
import static fr.enedis.teme.jquery.StdComparator.EQ;
import static fr.enedis.teme.jquery.StdComparator.GE;
import static fr.enedis.teme.jquery.StdComparator.GT;
import static fr.enedis.teme.jquery.StdComparator.ILIKE;
import static fr.enedis.teme.jquery.StdComparator.IN;
import static fr.enedis.teme.jquery.StdComparator.IS_NOT_NULL;
import static fr.enedis.teme.jquery.StdComparator.IS_NULL;
import static fr.enedis.teme.jquery.StdComparator.LE;
import static fr.enedis.teme.jquery.StdComparator.LIKE;
import static fr.enedis.teme.jquery.StdComparator.LT;
import static fr.enedis.teme.jquery.StdComparator.NE;
import static fr.enedis.teme.jquery.StdComparator.NOT_ILIKE;
import static fr.enedis.teme.jquery.StdComparator.NOT_IN;
import static fr.enedis.teme.jquery.StdComparator.NOT_LIKE;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class ComparisonSingleExpression implements ComparatorExpression {
	@NonNull
	private final DBComparator comparator;
	private final Object value; //nullable
	
	@Override
	public String sql(QueryParameterBuilder builder, Object operand) {
		return DBCallable.sql(comparator, builder, operand, value);
	}
	
	@Override
	public ComparatorExpression append(LogicalOperator op, ComparatorExpression exp) {
		return new ComparaisonExpressionGroup(op, this, exp);
	}

	public static final ComparisonSingleExpression equal(Object value) {
		return new ComparisonSingleExpression(EQ, value);
	}

	public static final ComparisonSingleExpression notEqual(Object value) {
		return new ComparisonSingleExpression(NE, value);
	}
	
	public static final ComparisonSingleExpression lessThan(Object value) {
		return new ComparisonSingleExpression(LT, value);
	}

	public static final ComparisonSingleExpression lessOrEqual(Object value) {
		return new ComparisonSingleExpression(LE, value);
	}

	public static final ComparisonSingleExpression greaterThan(Object value) {
		return new ComparisonSingleExpression(GT, value);
	}

	public static final ComparisonSingleExpression greaterOrEqual(Object value) {
		return new ComparisonSingleExpression(GE, value);
	}

	public static final ComparisonSingleExpression like(Object value) {
		return new ComparisonSingleExpression(LIKE, value);
	}
	
	public static final ComparisonSingleExpression iLike(Object value) {
		return new ComparisonSingleExpression(ILIKE, value);
	}

	public static final ComparisonSingleExpression notLike(Object value) {
		return new ComparisonSingleExpression(NOT_LIKE, value);
	}

	public static final ComparisonSingleExpression notILike(Object value) {
		return new ComparisonSingleExpression(NOT_ILIKE, value);
	}

	@SafeVarargs
	public static final <T> ComparisonSingleExpression in(@NonNull T... values) {
		return new ComparisonSingleExpression(IN, values);
	}
	@SafeVarargs
	public static final <T> ComparisonSingleExpression notIn(@NonNull T... values) {
		return new ComparisonSingleExpression(NOT_IN, values);
	}
	
	public static final ComparisonSingleExpression isNull() {
		return  new ComparisonSingleExpression(IS_NULL, null);
	}

	public static final ComparisonSingleExpression isNotNull() {
		return  new ComparisonSingleExpression(IS_NOT_NULL, null);
	}
	
	@Override
	public String toString() {
		return sql(addWithValue(), EMPTY_STRING);
	}
}

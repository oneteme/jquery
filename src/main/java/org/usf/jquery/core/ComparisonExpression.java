package org.usf.jquery.core;

import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.LogicalOperator.OR;
import static org.usf.jquery.core.Utils.hasSize;
import static org.usf.jquery.core.Validation.illegalArgumentIf;

import lombok.NonNull;

/**
 * 
 * @author u$f
 *
 */
public interface ComparisonExpression extends DBExpression, NestedSql {

	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		illegalArgumentIf(!hasSize(args, 1), "comparison takes one argument");
		return sql(builder, args[0]);
	}

	String sql(QueryParameterBuilder builder, Object left); // do change method order
	
	ComparisonExpression append(LogicalOperator op, ComparisonExpression exp);
	
	default ComparisonExpression and(ComparisonExpression exp) {
		return append(AND, exp);
	}

	default ComparisonExpression or(ComparisonExpression exp) {
		return append(OR, exp);
	}
	
	static ComparisonExpression equal(Object right) {
		return DBComparator.equal().expression(right);
	}

	static ComparisonExpression notEqual(Object right) {
		return DBComparator.notEqual().expression(right);
	}
	
	static ComparisonExpression lessThan(Object right) {
		return DBComparator.lessThan().expression(right);
	}

	static ComparisonExpression lessOrEqual(Object right) {
		return DBComparator.lessOrEqual().expression(right);
	}

	static ComparisonExpression greaterThan(Object right) {
		return DBComparator.greaterThan().expression(right);
	}

	static ComparisonExpression greaterOrEqual(Object right) {
		return DBComparator.greaterOrEqual().expression(right);
	}
	
	static ComparisonExpression like(Object right) {
		return DBComparator.like().expression(right);
	}
	
	static ComparisonExpression iLike(Object right) {
		return DBComparator.iLike().expression(right);
	}

	static ComparisonExpression notLike(Object right) {
		return DBComparator.notLike().expression(right);
	}

	static ComparisonExpression notILike(Object right) {
		return DBComparator.notILike().expression(right);
	}

	static ComparisonExpression isNull() {
		return DBComparator.isNull().expression(null);
	}

	static ComparisonExpression isNotNull() {
		return DBComparator.isNotNull().expression(null);
	}

	@SuppressWarnings("unchecked")
	static <T> ComparisonExpression in(@NonNull T... right) {
		return DBComparator.in().expression(right);
	}
	
	@SuppressWarnings("unchecked")
	static <T> ComparisonExpression notIn(@NonNull T... right) {
		return DBComparator.notIn().expression(right);
	}

}

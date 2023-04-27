package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Validation.illegalArgumentIf;
import static org.usf.jquery.core.Validation.requireLegalAlias;

import org.usf.jquery.core.CaseSingleColumnBuilder.WhenFilterBridge;

import lombok.NonNull;

@FunctionalInterface
public interface DBColumn extends DBObject, NestedSql {
	
	String sql(QueryParameterBuilder builder);
	
	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		illegalArgumentIf(nonNull(args), "DBColumn takes no arguments");
		return sql(builder);
	}

	@Override
	default boolean isAggregation() {
		return false;
	}

	default boolean isConstant() {
		return false;
	}

	default NamedColumn as(String name) {
		return new NamedColumn(this, requireLegalAlias(name));
	}

	// filters
	default ColumnSingleFilter equal(Object value) {
		return filter(DBComparator.equal(value));
	}

	default ColumnSingleFilter notEqual(Object value) {
		return filter(DBComparator.notEqual(value));
	}

	default ColumnSingleFilter greaterThan(Object value) {
		return filter(DBComparator.greaterThan(value));
	}

	default ColumnSingleFilter greaterOrEqual(Object value) {
		return filter(DBComparator.greaterOrEqual(value));
	}

	default ColumnSingleFilter lessThan(Object value) {
		return filter(DBComparator.lessThan(value));
	}

	default ColumnSingleFilter lessOrEqual(Object value) {
		return filter(DBComparator.lessOrEqual(value));
	}

	default ColumnSingleFilter like(Object value) {
		return filter(DBComparator.like(value));
	}

	default ColumnSingleFilter notLike(Object value) {
		return filter(DBComparator.notLike(value));
	}

	default ColumnSingleFilter ilike(Object value) {
		return filter(DBComparator.iLike(value));
	}

	default ColumnSingleFilter notILike(Object value) {
		return filter(DBComparator.notILike(value));
	}

	@SuppressWarnings("unchecked")
	default <T> ColumnSingleFilter in(T... values) {
		return filter(DBComparator.in(values));
	}

	@SuppressWarnings("unchecked")
	default <T> ColumnSingleFilter notIn(T... values) {
		return filter(DBComparator.notIn(values));
	}

	default ColumnSingleFilter isNull() {
		return filter(DBComparator.isNull());
	}

	default ColumnSingleFilter isNotNull() {
		return filter(DBComparator.isNotNull());
	}

	default ColumnSingleFilter filter(ComparisonExpression exp) {
		return new ColumnSingleFilter(this, exp);
	}

	// operations
	
	default OperationColumn plus(Object o) {
		return DBArithmetic.plus().args(this, o);
	}

	default OperationColumn minus(Object o) {
		return DBArithmetic.minus().args(this, o);
	}

	default OperationColumn multiply(Object o) {
		return DBArithmetic.multiply().args(this, o);
	}

	default OperationColumn divise(Object o) {
		return DBArithmetic.divise().args(this, o);
	}
	
	default OperationColumn mode(Object o) {
		return DBArithmetic.mode().args(this, o);
	}

	default OperationColumn pow(Object o) {
		return DBArithmetic.pow().args(this, o);
	}

	default WhenFilterBridge when(ComparisonExpression ex) {
		return new CaseSingleColumnBuilder(this).when(ex);
	}
	
	static DBColumn column(@NonNull String value) {
		return p-> value;
	}

	static DBColumn constant(Object value) {
		return new DBColumn() {
			
			@Override
			public String sql(QueryParameterBuilder arg) {
				return arg.formatValue(value);
			}
			
			@Override
			public boolean isConstant() {
				return true;
			}
		};
	}
	
	static boolean isColumnConstant(Object o) {
		return !(o instanceof DBColumn) || ((DBColumn)o).isConstant();
	}
}

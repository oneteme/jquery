package org.usf.jquery.core;

import static java.util.Optional.empty;
import static org.usf.jquery.core.JDBCType.AUTO;
import static org.usf.jquery.core.Validation.requireLegalVariable;
import static org.usf.jquery.core.Validation.requireNoArgs;

import java.util.Optional;
import java.util.function.Supplier;

import org.usf.jquery.core.CaseSingleColumnBuilder.WhenFilterBridge;

import lombok.NonNull;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface DBColumn extends DBObject, Typed, NestedSql {
	
	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		requireNoArgs(args, DBColumn.class::getSimpleName);
		return sql(builder);
	}
	
	String sql(QueryParameterBuilder builder);

	@Override
	default boolean isAggregation() {
		return false;
	}

	default boolean isConstant() {
		return false;
	}
	
	default JavaType javaType() {
		return AUTO;
	}

	default NamedColumn as(String name) {
		return new NamedColumn(this, requireLegalVariable(name));
	}
	
	default DBOrder order() {
		return order(null);
	}
	
	@Deprecated(forRemoval = false) //unsafe value
	default DBOrder order(String order) {
		return new DBOrder(this, order);
	}

	// filters
	default ColumnSingleFilter equal(Object value) {
		return filter(ComparisonExpression.equal(value));
	}

	default ColumnSingleFilter notEqual(Object value) {
		return filter(ComparisonExpression.notEqual(value));
	}

	default ColumnSingleFilter greaterThan(Object value) {
		return filter(ComparisonExpression.greaterThan(value));
	}

	default ColumnSingleFilter greaterOrEqual(Object value) {
		return filter(ComparisonExpression.greaterOrEqual(value));
	}

	default ColumnSingleFilter lessThan(Object value) {
		return filter(ComparisonExpression.lessThan(value));
	}

	default ColumnSingleFilter lessOrEqual(Object value) {
		return filter(ComparisonExpression.lessOrEqual(value));
	}

	default ColumnSingleFilter like(Object value) {
		return filter(ComparisonExpression.like(value));
	}

	default ColumnSingleFilter notLike(Object value) {
		return filter(ComparisonExpression.notLike(value));
	}

	default ColumnSingleFilter ilike(Object value) {
		return filter(ComparisonExpression.iLike(value));
	}

	default ColumnSingleFilter notILike(Object value) {
		return filter(ComparisonExpression.notILike(value));
	}

	@SuppressWarnings("unchecked")
	default <T> ColumnSingleFilter in(T... values) {
		return filter(ComparisonExpression.in(values));
	}

	@SuppressWarnings("unchecked")
	default <T> ColumnSingleFilter notIn(T... values) {
		return filter(ComparisonExpression.notIn(values));
	}

	default ColumnSingleFilter isNull() {
		return filter(ComparisonExpression.isNull());
	}

	default ColumnSingleFilter isNotNull() {
		return filter(ComparisonExpression.isNotNull());
	}

	default ColumnSingleFilter filter(ComparisonExpression exp) {
		return new ColumnSingleFilter(this, exp);
	}

	// operations
	
	default OperationColumn plus(Object o) {
		return Operator.plus().args(this, o);
	}

	default OperationColumn minus(Object o) {
		return Operator.minus().args(this, o);
	}

	default OperationColumn multiply(Object o) {
		return Operator.multiply().args(this, o);
	}

	default OperationColumn divise(Object o) {
		return Operator.divise().args(this, o);
	}
	
	default WhenFilterBridge when(ComparisonExpression ex) {
		return new CaseSingleColumnBuilder(this).when(ex);
	}
	
	static DBColumn column(@NonNull String value) {
		return p-> value;
	}

	static DBColumn constant(Object value) {
		return constant(()-> value);
	}

	static DBColumn constant(Supplier<Object> value) {
		return new DBColumn() {
			
			@Override
			public String sql(QueryParameterBuilder arg) {
				return arg.formatValue(value.get());
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
	

	static OperationColumn count() {
		return count(column("*"));
	}

	static OperationColumn count(Object arg) {
		return Operator.count().args(arg);
	}

	static OperationColumn min(Object arg) {
		return Operator.min().args(arg);
	}

	static OperationColumn max(Object arg) {
		return Operator.max().args(arg);
	}

	static OperationColumn sum(Object arg) {
		return Operator.sum().args(arg);
	}
	
	static OperationColumn avg(Object arg) {
		return Operator.avg().args(arg);
	}
	
	//numeric
	
	static OperationColumn abs(Object arg) {
		return Operator.abs().args(arg);
	}
	
	static OperationColumn sqrt(Object arg) {
		return Operator.sqrt().args(arg);
	}

	static OperationColumn trunc(Object arg) {
		return Operator.trunc().args(arg);
	}

	static OperationColumn ceil(Object arg) {
		return Operator.ceil().args(arg);
	}

	static OperationColumn floor(Object arg) {
		return Operator.floor().args(arg);
	}
	
	//string
	static OperationColumn trim(Object arg) {
		return Operator.trim().args(arg);
	}

	static OperationColumn length(Object arg) {
		return Operator.length().args(arg);
	}

	static OperationColumn upper(Object arg) {
		return Operator.upper().args(arg);
	}

	static OperationColumn lower(Object arg) {
		return Operator.lower().args(arg);
	}
	
	static OperationColumn substring(Object arg, int start, int length) {
		return Operator.substring().args(arg, start, length);
	}
	
	static Optional<OperationColumn> lookupColumnFunction() {
		return empty();
	}
}

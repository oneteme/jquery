package org.usf.jquery.core;

import static org.usf.jquery.core.QueryParameterBuilder.formatValue;
import static org.usf.jquery.core.SqlStringBuilder.member;
import static org.usf.jquery.core.Validation.requireLegalVariable;
import static org.usf.jquery.core.Validation.requireNoArgs;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.usf.jquery.core.CaseSingleColumnBuilder.WhenFilterBridge;
import org.usf.jquery.core.JavaType.Typed;

import lombok.NonNull;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface DBColumn extends DBObject, Typed, Groupable {
	
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
	
	@Override
	default Stream<DBColumn> groupKeys() {
		return Stream.of(this);
	}
	
	default JDBCType getType() {
		return null;
	}

	default NamedColumn as(String name) {
		return new NamedColumn(this, Objects.isNull(name) ? null : requireLegalVariable(name));
	}
	
	default DBOrder order() {
		return new DBOrder(this);
	}
	
	default DBOrder order(Order order) {
		return new DBOrder(this, order);
	}

	// filters
	default ColumnSingleFilter equal(Object value) {
		return filter(ComparisonExpression.eq(value));
	}

	default ColumnSingleFilter notEqual(Object value) {
		return filter(ComparisonExpression.ne(value));
	}

	default ColumnSingleFilter greaterThan(Object value) {
		return filter(ComparisonExpression.gt(value));
	}

	default ColumnSingleFilter greaterOrEqual(Object value) {
		return filter(ComparisonExpression.ge(value));
	}

	default ColumnSingleFilter lessThan(Object value) {
		return filter(ComparisonExpression.lt(value));
	}

	default ColumnSingleFilter lessOrEqual(Object value) {
		return filter(ComparisonExpression.le(value));
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

	default OperationColumn divide(Object o) {
		return Operator.divide().args(this, o);
	}
	
	default WhenFilterBridge when(ComparisonExpression ex) {
		return new CaseSingleColumnBuilder(this).when(ex);
	}
	
	static DBColumn column(@NonNull String value) {
		return b-> value;
	}

	static DBColumn allColumns(@NonNull DBView view) {
		return column(view, "*");
	}

	static DBColumn column(@NonNull DBView view, @NonNull String value) {
		return b-> member(b.view(view), value);
	}
	
	static DBColumn constant(Object value) {
		return constant(()-> value);
	}

	static DBColumn constant(Supplier<Object> value) {
		return new DBColumn() {
			
			@Override
			public String sql(QueryParameterBuilder arg) {
				return formatValue(value.get());
			}
			
			@Override
			public boolean isAggregation() {
				return false;
			}
			
			@Override
			public Stream<DBColumn> groupKeys() {
				return Stream.empty();
			}
		};
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
}

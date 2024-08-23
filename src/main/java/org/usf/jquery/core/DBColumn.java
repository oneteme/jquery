package org.usf.jquery.core;

import static org.usf.jquery.core.QueryParameterBuilder.formatValue;
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
	default ColumnSingleFilter eq(Object value) {
		return filter(ComparisonExpression.eq(value));
	}

	default ColumnSingleFilter ne(Object value) {
		return filter(ComparisonExpression.ne(value));
	}

	default ColumnSingleFilter gt(Object value) {
		return filter(ComparisonExpression.gt(value));
	}

	default ColumnSingleFilter ge(Object value) {
		return filter(ComparisonExpression.ge(value));
	}

	default ColumnSingleFilter lt(Object value) {
		return filter(ComparisonExpression.lt(value));
	}

	default ColumnSingleFilter le(Object value) {
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
		return Operator.plus().operation(this, o);
	}

	default OperationColumn minus(Object o) {
		return Operator.minus().operation(this, o);
	}

	default OperationColumn multiply(Object o) {
		return Operator.multiply().operation(this, o);
	}

	default OperationColumn divide(Object o) {
		return Operator.divide().operation(this, o);
	}
	
	default WhenFilterBridge when(ComparisonExpression ex) {
		return new CaseSingleColumnBuilder(this).when(ex);
	}
	
	static DBColumn column(@NonNull String value) {
		return b-> value;
	}

	static TaggableColumn allColumns(@NonNull DBView view) {
		 return ((DBColumn) b-> {
			b.view(view);
			return "*"; //avoid view.* as ""
		}).as(null);
	}
	
	static DBColumn constant(Object value) {
		return constant(()-> value);
	}

	static DBColumn constant(Supplier<Object> value) {
		return new DBColumn() {
			
			@Override
			public String sql(QueryParameterBuilder arg) {
				return formatValue(value.get()); //lazy 
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
		return Operator.count().operation(arg);
	}

	static OperationColumn min(Object arg) {
		return Operator.min().operation(arg);
	}

	static OperationColumn max(Object arg) {
		return Operator.max().operation(arg);
	}

	static OperationColumn sum(Object arg) {
		return Operator.sum().operation(arg);
	}
	
	static OperationColumn avg(Object arg) {
		return Operator.avg().operation(arg);
	}
	
	//numeric
	
	static OperationColumn abs(Object arg) {
		return Operator.abs().operation(arg);
	}
	
	static OperationColumn sqrt(Object arg) {
		return Operator.sqrt().operation(arg);
	}

	static OperationColumn trunc(Object arg) {
		return Operator.trunc().operation(arg);
	}

	static OperationColumn ceil(Object arg) {
		return Operator.ceil().operation(arg);
	}

	static OperationColumn floor(Object arg) {
		return Operator.floor().operation(arg);
	}
	
	//string
	static OperationColumn trim(Object arg) {
		return Operator.trim().operation(arg);
	}

	static OperationColumn length(Object arg) {
		return Operator.length().operation(arg);
	}

	static OperationColumn upper(Object arg) {
		return Operator.upper().operation(arg);
	}

	static OperationColumn lower(Object arg) {
		return Operator.lower().operation(arg);
	}
	
	static OperationColumn substring(Object arg, int start, int length) {
		return Operator.substring().operation(arg, start, length);
	}
}

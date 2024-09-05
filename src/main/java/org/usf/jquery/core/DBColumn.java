package org.usf.jquery.core;

import static org.usf.jquery.core.Order.ASC;
import static org.usf.jquery.core.Order.DESC;
import static org.usf.jquery.core.QueryVariables.formatValue;
import static org.usf.jquery.core.Utils.appendFirst;
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
	
	String sql(QueryVariables builder);
	
	@Override
	default String sql(QueryVariables builder, Object[] args) {
		requireNoArgs(args, DBColumn.class::getSimpleName);
		return sql(builder);
	}
	
	@Override
	default JDBCType getType() {
		return null;
	}

	@Override
	default boolean isAggregation() {
		return false;
	}
	
	@Override
	default Stream<DBColumn> groupKeys() {
		return Stream.of(this);
	}
	
	default ColumnProxy as(String name) {
		return as(name, null);
	}
	
	default ColumnProxy as(String name, JDBCType type) {
		return new ColumnProxy(this, type, Objects.isNull(name) ? null : requireLegalVariable(name));
	}
	
	// filters
	
	default ColumnSingleFilter eq(Object value) {
		return Comparator.eq().filter(this, value);
	}

	default ColumnSingleFilter ne(Object value) {
		return Comparator.ne().filter(this, value);
	}

	default ColumnSingleFilter lt(Object value) {
		return Comparator.lt().filter(this, value);
	}

	default ColumnSingleFilter le(Object value) {
		return Comparator.le().filter(this, value);
	}

	default ColumnSingleFilter gt(Object value) {
		return Comparator.gt().filter(this, value);
	}

	default ColumnSingleFilter ge(Object value) {
		return Comparator.ge().filter(this, value);
	}

	default ColumnSingleFilter between(Object min, Object max) { //included
		return Comparator.between().filter(this, min, max);
	}
	
	default ColumnSingleFilter like(Object value) {
		return Comparator.like().filter(this, value);
	}
	
	default ColumnSingleFilter startsLike(Object value) {
		return Comparator.startsLike().filter(this, value);
	}

	default ColumnSingleFilter endsLike(Object value) {
		return Comparator.endsLike().filter(this, value);
	}

	default ColumnSingleFilter contentLike(Object value) {
		return Comparator.contentLike().filter(this, value);
	}
	
	default ColumnSingleFilter startsNotLike(Object value) {
		return Comparator.startsNotLike().filter(this, value);
	}

	default ColumnSingleFilter endsNotLike(Object value) {
		return Comparator.endsNotLike().filter(this, value);
	}

	default ColumnSingleFilter contentNotLike(Object value) {
		return Comparator.contentNotLike().filter(this, value);
	}

	default ColumnSingleFilter notLike(Object value) {
		return Comparator.notLike().filter(this, value);
	}

	default ColumnSingleFilter ilike(Object value) {
		return Comparator.iLike().filter(this, value);
	}

	default ColumnSingleFilter notILike(Object value) {
		return Comparator.notILike().filter(this, value);
	}

	default ColumnSingleFilter isNull() {
		return Comparator.isNull().filter(this);
	}

	default ColumnSingleFilter notNull() {
		return Comparator.notNull().filter(this);
	}

	@SuppressWarnings("unchecked")
	default <T> ColumnSingleFilter in(T... arr) {
		return Comparator.in().filter(appendFirst(arr, this)); 
	}

	@SuppressWarnings("unchecked")
	default <T> ColumnSingleFilter notIn(T... arr) {
		return Comparator.notIn().filter(appendFirst(arr, this));
	}
	
	default ColumnSingleFilter filter(ComparisonExpression exp) {
		return new ColumnSingleFilter(this, exp);
	}

	// arithmetic operations
	
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
	
	//numeric functions
	
	default OperationColumn sqrt(Object o) {
		return Operator.sqrt().operation(this, o);
	}
	
	default OperationColumn exp(Object o) {
		return Operator.exp().operation(this, o);
	}
	
	default OperationColumn log(Object o) {
		return Operator.log().operation(this, o);
	}
	
	default OperationColumn abs(Object o) {
		return Operator.abs().operation(this, o);
	}

	default OperationColumn ceil(Object o) {
		return Operator.ceil().operation(this, o);
	}

	default OperationColumn floor(Object o) {
		return Operator.floor().operation(this, o);
	}

	default OperationColumn trunc(Object o) {
		return Operator.trunc().operation(this, o);
	}
	
	default OperationColumn round(Object o) {
		return Operator.round().operation(this, o);
	}
	
	default OperationColumn mod(Object o) {
		return Operator.mod().operation(this, o);
	}
	
	default OperationColumn pow(Object o) {
		return Operator.pow().operation(this, o);
	}
	
	//string functions

	default OperationColumn length() {
		return Operator.length().operation(this);
	}
	
	default OperationColumn trim() {
		return Operator.trim().operation(this);
	}

	default OperationColumn ltrim() {
		return Operator.ltrim().operation(this);
	}

	default OperationColumn rtrim() {
		return Operator.rtrim().operation(this);
	}
	
	default OperationColumn upper() {
		return Operator.upper().operation(this);
	}

	default OperationColumn lower() {
		return Operator.lower().operation(this);
	}
	
	default OperationColumn initcap() {
		return Operator.initcap().operation(this);
	}
	
	default OperationColumn reverse() {
		return Operator.reverse().operation(this);
	}
	
	default OperationColumn left(int n) {
		return Operator.left().operation(this, n);
	}
	
	default OperationColumn right(int n) {
		return Operator.pow().operation(this, n);
	}
	
	default OperationColumn replace(String oldValue, String newValue) {
		return Operator.replace().operation(this, oldValue, newValue);
	}
	
	default OperationColumn substring(int start, int end) {
		return Operator.substring().operation(this, start, end);
	}
	
	default OperationColumn concat(Object... str) {
		return Operator.concat().operation(appendFirst(str, this));
	}
	
	default OperationColumn lpad(int n, String value) {
		return Operator.lpad().operation(this, n, value);
	}
	
	default OperationColumn rpad(int n, String value) {
		return Operator.rpad().operation(this, n, value);
	}

	//temporal functions
	
	default OperationColumn year() {
		return Operator.year().operation(this);
	}
	
	default OperationColumn month() {
		return Operator.month().operation(this);
	}

	default OperationColumn week() {
		return Operator.week().operation(this);
	}
	
	default OperationColumn day() {
		return Operator.day().operation(this);
	}
	
	default OperationColumn dow() {
		return Operator.dow().operation(this);
	}
	
	default OperationColumn doy() {
		return Operator.doy().operation(this);
	}

	default OperationColumn hour() {
		return Operator.hour().operation(this);
	}

	default OperationColumn minute() {
		return Operator.minute().operation(this);
	}
	
	default OperationColumn second() {
		return Operator.second().operation(this);
	}
	
	default OperationColumn epoch() {
		return Operator.epoch().operation(this);
	}
	
	default OperationColumn yearMonth() {
		return Operator.yearMonth().operation(this);
	}
	
	//cast functions

	default OperationColumn varchar() {
		return Operator.varchar().operation(this);
	}
	
	default OperationColumn varchar(int size) {
		return Operator.varchar().operation(this, size);
	}
	
	default OperationColumn date() {
		return Operator.date().operation(this);
	}
	
	default OperationColumn timestamp() {
		return Operator.timestamp().operation(this);
	}
	
	default OperationColumn integer() {
		return Operator.integer().operation(this);
	}
	
	default OperationColumn bigint() {
		return Operator.bigint().operation(this);
	}
	
	default OperationColumn decimal() {
		return Operator.decimal().operation(this);
	}
	
	default OperationColumn decimal(int digit, int precision) {
		return Operator.decimal().operation(this, digit, precision);
	}

	//other functions
	
	default OperationColumn coalesce(Object o) {
		return Operator.coalesce().operation(this, o);
	}
	
	//aggregate functions

	default OperationColumn count() {
		return Operator.count().operation(this);
	}

	default OperationColumn min() {
		return Operator.min().operation(this);
	}

	default OperationColumn max() {
		return Operator.max().operation(this);
	}

	default OperationColumn sum() {
		return Operator.sum().operation(this);
	}
	
	default OperationColumn avg() {
		return Operator.avg().operation(this);
	}

	//pipe functions
	
	default OperationColumn over(Partition part) {
		return Operator.over().operation(this, part);
	}

	//orders

	default DBOrder order() {
		return order(null); //default
	}
	
	default DBOrder asc() {
		return order(ASC);
	}
	
	default DBOrder desc() {
		return order(DESC);
	}
	
	default DBOrder order(Order order) {
		return new DBOrder(this, order);
	}
	
	default WhenFilterBridge when(ComparisonExpression ex) {
		return new CaseSingleColumnBuilder(this).when(ex);
	}
	
	// constants
	
	static OperationColumn cdate() {
		return Operator.cdate().operation();
	}
	
	static OperationColumn ctime() {
		return Operator.ctime().operation();
	}
	
	static OperationColumn ctimestamp() {
		return Operator.ctimestamp().operation();
	}

	static OperationColumn countAll() {
		return Operator.count().operation(column("*"));
	}
	
	//window functions
	
	static OperationColumn rank() {
		return Operator.rank().operation();
	}
	
	static OperationColumn rowNumber() {
		return Operator.rowNumber().operation();
	}
	
	static OperationColumn denseRank() {
		return Operator.denseRank().operation();
	}
	
	static DBColumn column(@NonNull String value) {
		return b-> value;
	}

	static NamedColumn allColumns(@NonNull DBView view) {
		 return new ViewColumn("*", view, null, null) ; //TODO check this
	}
	
	static DBColumn constant(Object value) {
		return constant(()-> value);
	}

	static DBColumn constant(Supplier<Object> value) {
		return new DBColumn() {
			
			@Override
			public String sql(QueryVariables arg) {
				return formatValue(value.get()); //lazy 
			}
			
			@Override
			public Stream<DBColumn> groupKeys() {
				return Stream.empty();
			}
		};
	}
}

package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.OrderType.ASC;
import static org.usf.jquery.core.OrderType.DESC;
import static org.usf.jquery.core.Utils.appendFirst;
import static org.usf.jquery.core.Validation.requireLegalVariable;
import static org.usf.jquery.core.Validation.requireNoArgs;

import org.usf.jquery.core.JavaType.Typed;

/**
 * 
 * @author u$f
 *
 */
public interface DBColumn extends DBObject, Typed {
	
	void build(QueryBuilder query);
	
	@Override
	default void build(QueryBuilder query, Object... args) {
		requireNoArgs(args, DBColumn.class::getSimpleName);
		build(query);
	}
	
	default ColumnProxy as(String name) {
		return as(name, null);
	}
	
	default ColumnProxy as(String name, JDBCType type) {
		return new ColumnProxy(this, type, nonNull(name) ? requireLegalVariable(name) : null);
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
	
	default DBColumn plus(Object o) {
		return Operator.plus().operation(this, o);
	}

	default DBColumn minus(Object o) {
		return Operator.minus().operation(this, o);
	}

	default DBColumn multiply(Object o) {
		return Operator.multiply().operation(this, o);
	}

	default DBColumn divide(Object o) {
		return Operator.divide().operation(this, o);
	}
	
	//numeric functions
	
	default DBColumn sqrt() {
		return Operator.sqrt().operation(this);
	}
	
	default DBColumn exp() {
		return Operator.exp().operation(this);
	}
	
	default DBColumn log() {
		return Operator.log().operation(this);
	}

	default DBColumn log(int base) {
		return Operator.log().operation(this, base);
	}
	
	default DBColumn abs() {
		return Operator.abs().operation(this);
	}

	default DBColumn ceil() {
		return Operator.ceil().operation(this);
	}

	default DBColumn floor() {
		return Operator.floor().operation(this);
	}

	default DBColumn trunc() {
		return Operator.trunc().operation(this);
	}

	default DBColumn trunc(int digit) {
		return Operator.trunc().operation(this, digit);
	}
	
	default DBColumn round() {
		return Operator.round().operation(this);
	}
	
	default DBColumn round(int digit) {
		return Operator.round().operation(this, digit);
	}
	
	default DBColumn mod(int value) {
		return Operator.mod().operation(this, value);
	}
	
	default DBColumn pow(int value) {
		return Operator.pow().operation(this, value);
	}
	
	//string functions

	default DBColumn length() {
		return Operator.length().operation(this);
	}
	
	default DBColumn trim() {
		return Operator.trim().operation(this);
	}

	default DBColumn ltrim() {
		return Operator.ltrim().operation(this);
	}

	default DBColumn rtrim() {
		return Operator.rtrim().operation(this);
	}
	
	default DBColumn upper() {
		return Operator.upper().operation(this);
	}

	default DBColumn lower() {
		return Operator.lower().operation(this);
	}
	
	default DBColumn initcap() {
		return Operator.initcap().operation(this);
	}
	
	default DBColumn reverse() {
		return Operator.reverse().operation(this);
	}
	
	default DBColumn left(int n) {
		return Operator.left().operation(this, n);
	}
	
	default DBColumn right(int n) {
		return Operator.pow().operation(this, n);
	}
	
	default DBColumn replace(String oldValue, String newValue) {
		return Operator.replace().operation(this, oldValue, newValue);
	}
	
	default DBColumn substring(int start, int end) {
		return Operator.substring().operation(this, start, end);
	}
	
	default DBColumn concat(Object... str) {
		return Operator.concat().operation(appendFirst(str, this));
	}
	
	default DBColumn lpad(int n, String value) {
		return Operator.lpad().operation(this, n, value);
	}
	
	default DBColumn rpad(int n, String value) {
		return Operator.rpad().operation(this, n, value);
	}

	//temporal functions
	
	default DBColumn year() {
		return Operator.year().operation(this);
	}
	
	default DBColumn month() {
		return Operator.month().operation(this);
	}

	default DBColumn week() {
		return Operator.week().operation(this);
	}
	
	default DBColumn day() {
		return Operator.day().operation(this);
	}
	
	default DBColumn dow() {
		return Operator.dow().operation(this);
	}
	
	default DBColumn doy() {
		return Operator.doy().operation(this);
	}

	default DBColumn hour() {
		return Operator.hour().operation(this);
	}

	default DBColumn minute() {
		return Operator.minute().operation(this);
	}
	
	default DBColumn second() {
		return Operator.second().operation(this);
	}
	
	default DBColumn epoch() {
		return Operator.epoch().operation(this);
	}
	
	default DBColumn yearMonth() {
		return Operator.yearMonth().operation(this);
	}
	
	default DBColumn yearWeek() {
		return Operator.yearWeek().operation(this);
	}
	
	default DBColumn monthDay() {
		return Operator.monthDay().operation(this);
	}
	
	default DBColumn hourMinute() {
		return Operator.hourMinute().operation(this);
	}
	
	//cast functions

	default DBColumn varchar() {
		return Operator.varchar().operation(this);
	}
	
	default DBColumn varchar(int size) {
		return Operator.varchar().operation(this, size);
	}
	
	default DBColumn date() {
		return Operator.date().operation(this);
	}
	
	default DBColumn timestamp() {
		return Operator.timestamp().operation(this);
	}
	
	default DBColumn integer() {
		return Operator.integer().operation(this);
	}
	
	default DBColumn bigint() {
		return Operator.bigint().operation(this);
	}
	
	default DBColumn decimal() {
		return Operator.decimal().operation(this);
	}
	
	default DBColumn decimal(int digit, int precision) {
		return Operator.decimal().operation(this, digit, precision);
	}

	//other functions
	
	default DBColumn distinct() {
		return Operator.distinct().operation(this);
	}
	
	default DBColumn coalesce(Object o) {
		return Operator.coalesce().operation(this, o);
	}
	
	//aggregate functions

	default DBColumn count() {
		return Operator.count().operation(this);
	}

	default DBColumn min() {
		return Operator.min().operation(this);
	}

	default DBColumn max() {
		return Operator.max().operation(this);
	}

	default DBColumn sum() {
		return Operator.sum().operation(this);
	}
	
	default DBColumn avg() {
		return Operator.avg().operation(this);
	}

	//pipe functions
	
	default DBColumn over(DBColumn[] cols, DBOrder[] orders) {
		return over(new Partition(cols, orders));
	}
	
	default DBColumn over(Partition part) {
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
	
	default DBOrder order(OrderType order) {
		return new DBOrder(this, order);
	}
	
	default SingleCaseColumnBuilder beginCase() {
		return new SingleCaseColumnBuilder(this);
	}
	
	// constants
	
	static DBColumn cdate() {
		return Operator.cdate().operation();
	}
	
	static DBColumn ctime() {
		return Operator.ctime().operation();
	}
	
	static DBColumn ctimestamp() {
		return Operator.ctimestamp().operation();
	}

	static DBColumn countAll(DBView... view) {
		return Operator.count().operation(allColumns(view));
	}
	
	//window functions
	
	static DBColumn rank() {
		return Operator.rank().operation();
	}
	
	static DBColumn rowNumber() {
		return Operator.rowNumber().operation();
	}
	
	static DBColumn denseRank() {
		return Operator.denseRank().operation();
	}
	
	static ViewColumn column(String value) {
		return new ViewColumn(value, null, null, null);
	}

	static ViewColumn column(String value, DBView view) {
		return new ViewColumn(value, view, null, null);
	}

	static ViewColumn column(String value, DBView view, JDBCType type) {
		return new ViewColumn(value, view, type, null);
	}

	static ViewColumn column(String value, JDBCType type) {
		return new ViewColumn(value, null, type, null);
	}
	
	static ViewColumn column(String value, DBView view, JDBCType type, String tag) {
		return new ViewColumn(value, view, type, tag);
	}

	static AllColumns allColumns(DBView... views) {
		return new AllColumns(views);
	}
	
	static ValueColumn constant(Object value) {
		return constant(value, JDBCType.typeOf(value).orElse(null));
	}

	static ValueColumn constant(Object value, JDBCType type) {
		return new ValueColumn(value, type);
	}

	static ValueColumn constant(JDBCType type, Adjuster<Object> adj) {
		return new ValueColumn(null, type, adj);
	}
}

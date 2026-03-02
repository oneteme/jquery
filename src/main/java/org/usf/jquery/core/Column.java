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
public interface Column extends DBObject, Typed {
	
	void build(QueryBuilder query);
	
	@Override
	default void build(QueryBuilder query, Object... args) {
		requireNoArgs(args, Column.class::getSimpleName);
		build(query);
	}
	
	default ColumnProxy as(JDBCType type) {
		throw new UnsupportedOperationException("not impl");
	}
	
	default ColumnProxy as(String name) {
		return as(name, null);
	}
	
	default ColumnProxy as(String name, JDBCType type) {
		return new ColumnProxy(this, type, nonNull(name) ? requireLegalVariable(name) : null);
	}
	
	// filters
	
	default SimpleCriteria eq(Object value) {
		return Comparator.eq().filter(this, value);
	}

	default SimpleCriteria ne(Object value) {
		return Comparator.ne().filter(this, value);
	}

	default SimpleCriteria lt(Object value) {
		return Comparator.lt().filter(this, value);
	}

	default SimpleCriteria le(Object value) {
		return Comparator.le().filter(this, value);
	}

	default SimpleCriteria gt(Object value) {
		return Comparator.gt().filter(this, value);
	}

	default SimpleCriteria ge(Object value) {
		return Comparator.ge().filter(this, value);
	}

	default SimpleCriteria between(Object min, Object max) { //included
		return Comparator.between().filter(this, min, max);
	}
	
	default SimpleCriteria like(Object value) {
		return Comparator.like().filter(this, value);
	}
	
	default SimpleCriteria startsLike(Object value) {
		return Comparator.startsLike().filter(this, value);
	}

	default SimpleCriteria endsLike(Object value) {
		return Comparator.endsLike().filter(this, value);
	}

	default SimpleCriteria contentLike(Object value) {
		return Comparator.contentLike().filter(this, value);
	}
	
	default SimpleCriteria startsNotLike(Object value) {
		return Comparator.startsNotLike().filter(this, value);
	}

	default SimpleCriteria endsNotLike(Object value) {
		return Comparator.endsNotLike().filter(this, value);
	}

	default SimpleCriteria contentNotLike(Object value) {
		return Comparator.contentNotLike().filter(this, value);
	}

	default SimpleCriteria notLike(Object value) {
		return Comparator.notLike().filter(this, value);
	}

	default SimpleCriteria ilike(Object value) {
		return Comparator.iLike().filter(this, value);
	}

	default SimpleCriteria notILike(Object value) {
		return Comparator.notILike().filter(this, value);
	}

	default SimpleCriteria isNull() {
		return Comparator.isNull().filter(this);
	}

	default SimpleCriteria notNull() {
		return Comparator.notNull().filter(this);
	}

	@SuppressWarnings("unchecked")
	default <T> SimpleCriteria in(T... arr) {
		return Comparator.in().filter(appendFirst(arr, this)); 
	}

	@SuppressWarnings("unchecked")
	default <T> SimpleCriteria notIn(T... arr) {
		return Comparator.notIn().filter(appendFirst(arr, this));
	}
	
	default SimpleCriteria filter(Predicate exp) {
		return new SimpleCriteria(this, exp);
	}
	
	// arithmetic operations
	
	default Column plus(Object o) {
		return Operator.plus().operation(this, o);
	}

	default Column minus(Object o) {
		return Operator.minus().operation(this, o);
	}

	default Column multiply(Object o) {
		return Operator.multiply().operation(this, o);
	}

	default Column divide(Object o) {
		return Operator.divide().operation(this, o);
	}
	
	//numeric functions
	
	default Column sqrt() {
		return Operator.sqrt().operation(this);
	}
	
	default Column exp() {
		return Operator.exp().operation(this);
	}
	
	default Column log() {
		return Operator.log().operation(this);
	}

	default Column log(int base) {
		return Operator.log().operation(this, base);
	}
	
	default Column abs() {
		return Operator.abs().operation(this);
	}

	default Column ceil() {
		return Operator.ceil().operation(this);
	}

	default Column floor() {
		return Operator.floor().operation(this);
	}

	default Column trunc() {
		return Operator.trunc().operation(this);
	}

	default Column trunc(int digit) {
		return Operator.trunc().operation(this, digit);
	}
	
	default Column round() {
		return Operator.round().operation(this);
	}
	
	default Column round(int digit) {
		return Operator.round().operation(this, digit);
	}
	
	default Column mod(int value) {
		return Operator.mod().operation(this, value);
	}
	
	default Column pow(int value) {
		return Operator.pow().operation(this, value);
	}

	//bitwise functions
	
	default Column bitAnd(int value) {
		return Operator.bitAnd().operation(this, value);
	}
	
	default Column bitOr(int value) {
		return Operator.bitOr().operation(this, value);
	}
	
	default Column bitXor(int value) {
		return Operator.bitXor().operation(this, value);
	}
	
	default Column bitNot(int value) {
		return Operator.bitNot().operation(this, value);
	}
	
	default Column bitShiftLeft(int value) {
		return Operator.bitShiftLeft().operation(this, value);
	}
	
	default Column bitShiftRight(int value) {
		return Operator.bitShiftRight().operation(this, value);
	}
	
	//string functions

	default Column length() {
		return Operator.length().operation(this);
	}
	
	default Column trim() {
		return Operator.trim().operation(this);
	}

	default Column ltrim() {
		return Operator.ltrim().operation(this);
	}

	default Column rtrim() {
		return Operator.rtrim().operation(this);
	}
	
	default Column upper() {
		return Operator.upper().operation(this);
	}

	default Column lower() {
		return Operator.lower().operation(this);
	}
	
	default Column initcap() {
		return Operator.initcap().operation(this);
	}
	
	default Column reverse() {
		return Operator.reverse().operation(this);
	}
	
	default Column left(int n) {
		return Operator.left().operation(this, n);
	}
	
	default Column right(int n) {
		return Operator.pow().operation(this, n);
	}
	
	default Column replace(String oldValue, String newValue) {
		return Operator.replace().operation(this, oldValue, newValue);
	}
	
	default Column substring(int start, int end) {
		return Operator.substring().operation(this, start, end);
	}
	
	default Column concat(Object... str) {
		return Operator.concat().operation(appendFirst(str, this));
	}
	
	default Column lpad(int n, String value) {
		return Operator.lpad().operation(this, n, value);
	}
	
	default Column rpad(int n, String value) {
		return Operator.rpad().operation(this, n, value);
	}

	//temporal functions
	
	default Column year() {
		return Operator.year().operation(this);
	}
	
	default Column month() {
		return Operator.month().operation(this);
	}

	default Column week() {
		return Operator.week().operation(this);
	}
	
	default Column day() {
		return Operator.day().operation(this);
	}
	
	default Column dow() {
		return Operator.dow().operation(this);
	}
	
	default Column doy() {
		return Operator.doy().operation(this);
	}

	default Column hour() {
		return Operator.hour().operation(this);
	}

	default Column minute() {
		return Operator.minute().operation(this);
	}
	
	default Column second() {
		return Operator.second().operation(this);
	}
	
	default Column epoch() {
		return Operator.epoch().operation(this);
	}
	
	default Column yearMonth() {
		return Operator.yearMonth().operation(this);
	}
	
	default Column yearWeek() {
		return Operator.yearWeek().operation(this);
	}
	
	default Column monthDay() {
		return Operator.monthDay().operation(this);
	}
	
	default Column hourMinute() {
		return Operator.hourMinute().operation(this);
	}
	
	//cast functions

	default Column varchar() {
		return Operator.varchar().operation(this);
	}
	
	default Column varchar(int size) {
		return Operator.varchar().operation(this, size);
	}
	
	default Column date() {
		return Operator.date().operation(this);
	}
	
	default Column timestamp() {
		return Operator.timestamp().operation(this);
	}
	
	default Column integer() {
		return Operator.integer().operation(this);
	}
	
	default Column bigint() {
		return Operator.bigint().operation(this);
	}
	
	default Column decimal() {
		return Operator.decimal().operation(this);
	}
	
	default Column decimal(int digit, int precision) {
		return Operator.decimal().operation(this, digit, precision);
	}

	//other functions
	
	default Column distinct() {
		return Operator.distinct().operation(this);
	}
	
	default Column coalesce(Object o) {
		return Operator.coalesce().operation(this, o);
	}
	
	//aggregate functions

	default Column count() {
		return Operator.count().operation(this);
	}

	default Column min() {
		return Operator.min().operation(this);
	}

	default Column max() {
		return Operator.max().operation(this);
	}

	default Column sum() {
		return Operator.sum().operation(this);
	}
	
	default Column avg() {
		return Operator.avg().operation(this);
	}

	//pipe functions
	
	default Column over(Column[] cols, Order[] orders) {
		return over(new Partition(cols, orders));
	}
	
	default Column over(Partition part) {
		return Operator.over().operation(this, part);
	}

	//orders

	default Order order() {
		return order(null); //default
	}
	
	default Order asc() {
		return order(ASC);
	}
	
	default Order desc() {
		return order(DESC);
	}
	
	default Order order(OrderType order) {
		return new Order(this, order);
	}
	
	default CaseColumnBuilder toCase() {
		return new CaseColumnBuilder(this);
	}
	
	static CaseColumnBuilder beginCase() {
		return new CaseColumnBuilder(); //no column, append filter only
	}
	
	// constants
	
	static Column cdate() {
		return Operator.cdate().operation();
	}
	
	static Column ctime() {
		return Operator.ctime().operation();
	}
	
	static Column ctimestamp() {
		return Operator.ctimestamp().operation();
	}

	static Column countAll(DBView... view) {
		return Operator.count().operation(allColumns(view));
	}
	
	//window functions
	
	static Column rank() {
		return Operator.rank().operation();
	}
	
	static Column rowNumber() {
		return Operator.rowNumber().operation();
	}
	
	static Column denseRank() {
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

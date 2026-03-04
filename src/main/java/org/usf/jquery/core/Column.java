package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Comparators.STD_COMPARTORS;
import static org.usf.jquery.core.Operators.STD_OPERATORS;
import static org.usf.jquery.core.OrderType.ASC;
import static org.usf.jquery.core.OrderType.DESC;
import static org.usf.jquery.core.Stores.getCurrentStore;
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
		return comparators().eq().filter(this, value);
	}

	default SimpleCriteria ne(Object value) {
		return comparators().ne().filter(this, value);
	}

	default SimpleCriteria lt(Object value) {
		return comparators().lt().filter(this, value);
	}

	default SimpleCriteria le(Object value) {
		return comparators().le().filter(this, value);
	}

	default SimpleCriteria gt(Object value) {
		return comparators().gt().filter(this, value);
	}

	default SimpleCriteria ge(Object value) {
		return comparators().ge().filter(this, value);
	}

	default SimpleCriteria between(Object min, Object max) { //included
		return comparators().between().filter(this, min, max);
	}
	
	default SimpleCriteria like(Object value) {
		return comparators().like().filter(this, value);
	}
	
	default SimpleCriteria startsLike(Object value) {
		return comparators().startsLike().filter(this, value);
	}

	default SimpleCriteria endsLike(Object value) {
		return comparators().endsLike().filter(this, value);
	}

	default SimpleCriteria contentLike(Object value) {
		return comparators().contentLike().filter(this, value);
	}
	
	default SimpleCriteria startsNotLike(Object value) {
		return comparators().startsNotLike().filter(this, value);
	}

	default SimpleCriteria endsNotLike(Object value) {
		return comparators().endsNotLike().filter(this, value);
	}

	default SimpleCriteria contentNotLike(Object value) {
		return comparators().contentNotLike().filter(this, value);
	}

	default SimpleCriteria notLike(Object value) {
		return comparators().notLike().filter(this, value);
	}

	default SimpleCriteria ilike(Object value) {
		return comparators().iLike().filter(this, value);
	}

	default SimpleCriteria notILike(Object value) {
		return comparators().notILike().filter(this, value);
	}

	default SimpleCriteria isNull() {
		return comparators().isNull().filter(this);
	}

	default SimpleCriteria notNull() {
		return comparators().notNull().filter(this);
	}

	@SuppressWarnings("unchecked")
	default <T> SimpleCriteria in(T... arr) {
		return comparators().in().filter(appendFirst(arr, this)); 
	}

	@SuppressWarnings("unchecked")
	default <T> SimpleCriteria notIn(T... arr) {
		return comparators().notIn().filter(appendFirst(arr, this));
	}
	
	default SimpleCriteria filter(Predicate exp) {
		return new SimpleCriteria(this, exp);
	}
	
	// arithmetic operations
	
	default Column plus(Object o) {
		return operators().plus().operation(this, o);
	}

	default Column minus(Object o) {
		return operators().minus().operation(this, o);
	}

	default Column multiply(Object o) {
		return operators().multiply().operation(this, o);
	}

	default Column divide(Object o) {
		return operators().divide().operation(this, o);
	}
	
	//numeric functions
	
	default Column sqrt() {
		return operators().sqrt().operation(this);
	}
	
	default Column exp() {
		return operators().exp().operation(this);
	}
	
	default Column log() {
		return operators().log().operation(this);
	}

	default Column log(int base) {
		return operators().log().operation(this, base);
	}
	
	default Column abs() {
		return operators().abs().operation(this);
	}

	default Column ceil() {
		return operators().ceil().operation(this);
	}

	default Column floor() {
		return operators().floor().operation(this);
	}

	default Column trunc() {
		return operators().trunc().operation(this);
	}

	default Column trunc(int digit) {
		return operators().trunc().operation(this, digit);
	}
	
	default Column round() {
		return operators().round().operation(this);
	}
	
	default Column round(int digit) {
		return operators().round().operation(this, digit);
	}
	
	default Column mod(int value) {
		return operators().mod().operation(this, value);
	}
	
	default Column pow(int value) {
		return operators().pow().operation(this, value);
	}

	//bitwise functions
	
	default Column bitAnd(int value) {
		return operators().bitAnd().operation(this, value);
	}
	
	default Column bitOr(int value) {
		return operators().bitOr().operation(this, value);
	}
	
	default Column bitXor(int value) {
		return operators().bitXor().operation(this, value);
	}
	
	default Column bitNot(int value) {
		return operators().bitNot().operation(this, value);
	}
	
	default Column bitShiftLeft(int value) {
		return operators().bitShiftLeft().operation(this, value);
	}
	
	default Column bitShiftRight(int value) {
		return operators().bitShiftRight().operation(this, value);
	}
	
	//string functions

	default Column length() {
		return operators().length().operation(this);
	}
	
	default Column trim() {
		return operators().trim().operation(this);
	}

	default Column ltrim() {
		return operators().ltrim().operation(this);
	}

	default Column rtrim() {
		return operators().rtrim().operation(this);
	}
	
	default Column upper() {
		return operators().upper().operation(this);
	}

	default Column lower() {
		return operators().lower().operation(this);
	}
	
	default Column initcap() {
		return operators().initcap().operation(this);
	}
	
	default Column reverse() {
		return operators().reverse().operation(this);
	}
	
	default Column left(int n) {
		return operators().left().operation(this, n);
	}
	
	default Column right(int n) {
		return operators().pow().operation(this, n);
	}
	
	default Column replace(String oldValue, String newValue) {
		return operators().replace().operation(this, oldValue, newValue);
	}
	
	default Column substring(int start, int end) {
		return operators().substring().operation(this, start, end);
	}
	
	default Column concat(Object... str) {
		return operators().concat().operation(appendFirst(str, this));
	}
	
	default Column lpad(int n, String value) {
		return operators().lpad().operation(this, n, value);
	}
	
	default Column rpad(int n, String value) {
		return operators().rpad().operation(this, n, value);
	}

	//temporal functions
	
	default Column year() {
		return operators().year().operation(this);
	}
	
	default Column month() {
		return operators().month().operation(this);
	}

	default Column week() {
		return operators().week().operation(this);
	}
	
	default Column day() {
		return operators().day().operation(this);
	}
	
	default Column dow() {
		return operators().dow().operation(this);
	}
	
	default Column doy() {
		return operators().doy().operation(this);
	}

	default Column hour() {
		return operators().hour().operation(this);
	}

	default Column minute() {
		return operators().minute().operation(this);
	}
	
	default Column second() {
		return operators().second().operation(this);
	}
	
	default Column epoch() {
		return operators().epoch().operation(this);
	}
	
	default Column yearMonth() {
		return operators().yearMonth().operation(this);
	}
	
	default Column yearWeek() {
		return operators().yearWeek().operation(this);
	}
	
	default Column monthDay() {
		return operators().monthDay().operation(this);
	}
	
	default Column hourMinute() {
		return operators().hourMinute().operation(this);
	}
	
	//cast functions

	default Column varchar() {
		return operators().varchar().operation(this);
	}
	
	default Column varchar(int size) {
		return operators().varchar().operation(this, size);
	}
	
	default Column date() {
		return operators().date().operation(this);
	}
	
	default Column timestamp() {
		return operators().timestamp().operation(this);
	}
	
	default Column integer() {
		return operators().integer().operation(this);
	}
	
	default Column bigint() {
		return operators().bigint().operation(this);
	}
	
	default Column decimal() {
		return operators().decimal().operation(this);
	}
	
	default Column decimal(int digit, int precision) {
		return operators().decimal().operation(this, digit, precision);
	}

	//other functions
	
	default Column distinct() {
		return operators().distinct().operation(this);
	}
	
	default Column coalesce(Object o) {
		return operators().coalesce().operation(this, o);
	}
	
	//aggregate functions

	default Column count() {
		return operators().count().operation(this);
	}

	default Column min() {
		return operators().min().operation(this);
	}

	default Column max() {
		return operators().max().operation(this);
	}

	default Column sum() {
		return operators().sum().operation(this);
	}
	
	default Column avg() {
		return operators().avg().operation(this);
	}

	//pipe functions
	
	default Column over(Column[] cols, Order[] orders) {
		return over(new Partition(cols, orders));
	}
	
	default Column over(Partition part) {
		return operators().over().operation(this, part);
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
		return operators().cdate().operation();
	}
	
	static Column ctime() {
		return operators().ctime().operation();
	}
	
	static Column ctimestamp() {
		return operators().ctimestamp().operation();
	}

	static Column countAll(DBView... view) {
		return operators().count().operation(allColumns(view));
	}
	
	//window functions
	
	static Column rank() {
		return operators().rank().operation();
	}
	
	static Column rowNumber() {
		return operators().rowNumber().operation();
	}
	
	static Column denseRank() {
		return operators().denseRank().operation();
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
	
	private static Operators operators() {		
		var store = getCurrentStore();
		return nonNull(store) ? store.operators() : STD_OPERATORS;
	}
	
	private static Comparators comparators() {
		var store = getCurrentStore();
		return nonNull(store) ? store.comparators() : STD_COMPARTORS;
	}
}

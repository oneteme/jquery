package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Dialect.DEFAULT_META;
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
		return dialect().eq().filter(this, value);
	}

	default SimpleCriteria ne(Object value) {
		return dialect().ne().filter(this, value);
	}

	default SimpleCriteria lt(Object value) {
		return dialect().lt().filter(this, value);
	}

	default SimpleCriteria le(Object value) {
		return dialect().le().filter(this, value);
	}

	default SimpleCriteria gt(Object value) {
		return dialect().gt().filter(this, value);
	}

	default SimpleCriteria ge(Object value) {
		return dialect().ge().filter(this, value);
	}

	default SimpleCriteria between(Object min, Object max) { //included
		return dialect().between().filter(this, min, max);
	}
	
	default SimpleCriteria like(Object value) {
		return dialect().like().filter(this, value);
	}
	
	default SimpleCriteria startsLike(Object value) {
		return dialect().startsLike().filter(this, value);
	}

	default SimpleCriteria endsLike(Object value) {
		return dialect().endsLike().filter(this, value);
	}

	default SimpleCriteria contentLike(Object value) {
		return dialect().contentLike().filter(this, value);
	}
	
	default SimpleCriteria startsNotLike(Object value) {
		return dialect().startsNotLike().filter(this, value);
	}

	default SimpleCriteria endsNotLike(Object value) {
		return dialect().endsNotLike().filter(this, value);
	}

	default SimpleCriteria contentNotLike(Object value) {
		return dialect().contentNotLike().filter(this, value);
	}

	default SimpleCriteria notLike(Object value) {
		return dialect().notLike().filter(this, value);
	}

	default SimpleCriteria ilike(Object value) {
		return dialect().iLike().filter(this, value);
	}

	default SimpleCriteria notILike(Object value) {
		return dialect().notILike().filter(this, value);
	}

	default SimpleCriteria isNull() {
		return dialect().isNull().filter(this);
	}

	default SimpleCriteria notNull() {
		return dialect().notNull().filter(this);
	}

	@SuppressWarnings("unchecked")
	default <T> SimpleCriteria in(T... arr) {
		return dialect().in().filter(appendFirst(arr, this)); 
	}

	@SuppressWarnings("unchecked")
	default <T> SimpleCriteria notIn(T... arr) {
		return dialect().notIn().filter(appendFirst(arr, this));
	}
	
	default SimpleCriteria filter(Predicate exp) {
		return new SimpleCriteria(this, exp);
	}
	
	// arithmetic operations
	
	default Column plus(Object o) {
		return dialect().plus().operation(this, o);
	}

	default Column minus(Object o) {
		return dialect().minus().operation(this, o);
	}

	default Column multiply(Object o) {
		return dialect().multiply().operation(this, o);
	}

	default Column divide(Object o) {
		return dialect().divide().operation(this, o);
	}
	
	//numeric functions
	
	default Column sqrt() {
		return dialect().sqrt().operation(this);
	}
	
	default Column exp() {
		return dialect().exp().operation(this);
	}
	
	default Column log() {
		return dialect().log().operation(this);
	}

	default Column log(int base) {
		return dialect().log().operation(this, base);
	}
	
	default Column abs() {
		return dialect().abs().operation(this);
	}

	default Column ceil() {
		return dialect().ceil().operation(this);
	}

	default Column floor() {
		return dialect().floor().operation(this);
	}

	default Column trunc() {
		return dialect().trunc().operation(this);
	}

	default Column trunc(int digit) {
		return dialect().trunc().operation(this, digit);
	}
	
	default Column round() {
		return dialect().round().operation(this);
	}
	
	default Column round(int digit) {
		return dialect().round().operation(this, digit);
	}
	
	default Column mod(int value) {
		return dialect().mod().operation(this, value);
	}
	
	default Column pow(int value) {
		return dialect().pow().operation(this, value);
	}

	//bitwise functions
	
	default Column bitAnd(int value) {
		return dialect().bitAnd().operation(this, value);
	}
	
	default Column bitOr(int value) {
		return dialect().bitOr().operation(this, value);
	}
	
	default Column bitXor(int value) {
		return dialect().bitXor().operation(this, value);
	}
	
	default Column bitNot(int value) {
		return dialect().bitNot().operation(this, value);
	}
	
	default Column bitShiftLeft(int value) {
		return dialect().bitShiftLeft().operation(this, value);
	}
	
	default Column bitShiftRight(int value) {
		return dialect().bitShiftRight().operation(this, value);
	}
	
	//string functions

	default Column length() {
		return dialect().length().operation(this);
	}
	
	default Column trim() {
		return dialect().trim().operation(this);
	}

	default Column ltrim() {
		return dialect().ltrim().operation(this);
	}

	default Column rtrim() {
		return dialect().rtrim().operation(this);
	}
	
	default Column upper() {
		return dialect().upper().operation(this);
	}

	default Column lower() {
		return dialect().lower().operation(this);
	}
	
	default Column initcap() {
		return dialect().initcap().operation(this);
	}
	
	default Column reverse() {
		return dialect().reverse().operation(this);
	}
	
	default Column left(int n) {
		return dialect().left().operation(this, n);
	}
	
	default Column right(int n) {
		return dialect().pow().operation(this, n);
	}
	
	default Column replace(String oldValue, String newValue) {
		return dialect().replace().operation(this, oldValue, newValue);
	}
	
	default Column substring(int start, int end) {
		return dialect().substring().operation(this, start, end);
	}
	
	default Column concat(Object... str) {
		return dialect().concat().operation(appendFirst(str, this));
	}
	
	default Column lpad(int n, String value) {
		return dialect().lpad().operation(this, n, value);
	}
	
	default Column rpad(int n, String value) {
		return dialect().rpad().operation(this, n, value);
	}

	//temporal functions
	
	default Column year() {
		return dialect().year().operation(this);
	}
	
	default Column month() {
		return dialect().month().operation(this);
	}

	default Column week() {
		return dialect().week().operation(this);
	}
	
	default Column day() {
		return dialect().day().operation(this);
	}
	
	default Column dow() {
		return dialect().dow().operation(this);
	}
	
	default Column doy() {
		return dialect().doy().operation(this);
	}

	default Column hour() {
		return dialect().hour().operation(this);
	}

	default Column minute() {
		return dialect().minute().operation(this);
	}
	
	default Column second() {
		return dialect().second().operation(this);
	}
	
	default Column epoch() {
		return dialect().epoch().operation(this);
	}
	
	default Column yearMonth() {
		return dialect().yearMonth().operation(this);
	}
	
	default Column yearWeek() {
		return dialect().yearWeek().operation(this);
	}
	
	default Column monthDay() {
		return dialect().monthDay().operation(this);
	}
	
	default Column hourMinute() {
		return dialect().hourMinute().operation(this);
	}
	
	//cast functions

	default Column varchar() {
		return dialect().varchar().operation(this);
	}
	
	default Column varchar(int size) {
		return dialect().varchar().operation(this, size);
	}
	
	default Column date() {
		return dialect().date().operation(this);
	}
	
	default Column timestamp() {
		return dialect().timestamp().operation(this);
	}
	
	default Column integer() {
		return dialect().integer().operation(this);
	}
	
	default Column bigint() {
		return dialect().bigint().operation(this);
	}
	
	default Column decimal() {
		return dialect().decimal().operation(this);
	}
	
	default Column decimal(int digit, int precision) {
		return dialect().decimal().operation(this, digit, precision);
	}

	//other functions
	
	default Column distinct() {
		return dialect().distinct().operation(this);
	}
	
	default Column coalesce(Object o) {
		return dialect().coalesce().operation(this, o);
	}
	
	//aggregate functions

	default Column count() {
		return dialect().count().operation(this);
	}

	default Column min() {
		return dialect().min().operation(this);
	}

	default Column max() {
		return dialect().max().operation(this);
	}

	default Column sum() {
		return dialect().sum().operation(this);
	}
	
	default Column avg() {
		return dialect().avg().operation(this);
	}

	//pipe functions
	
	default Column over(Column[] cols, Order[] orders) {
		return over(new Partition(cols, orders));
	}
	
	default Column over(Partition part) {
		return dialect().over().operation(this, part);
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
		return dialect().cdate().operation();
	}
	
	static Column ctime() {
		return dialect().ctime().operation();
	}
	
	static Column ctimestamp() {
		return dialect().ctimestamp().operation();
	}

	static Column countAll(DBView... view) {
		return dialect().count().operation(allColumns(view));
	}
	
	//window functions
	
	static Column rank() {
		return dialect().rank().operation();
	}
	
	static Column rowNumber() {
		return dialect().rowNumber().operation();
	}
	
	static Column denseRank() {
		return dialect().denseRank().operation();
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
	
	private static Dialect dialect() {		
		var store = getCurrentStore();
		return nonNull(store) ? store.dialect() : DEFAULT_META;
	}
}

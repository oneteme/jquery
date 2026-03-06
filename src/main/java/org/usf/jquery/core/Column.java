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
		return dialect().eq().invoke(this, value);
	}

	default SimpleCriteria ne(Object value) {
		return dialect().ne().invoke(this, value);
	}

	default SimpleCriteria lt(Object value) {
		return dialect().lt().invoke(this, value);
	}

	default SimpleCriteria le(Object value) {
		return dialect().le().invoke(this, value);
	}

	default SimpleCriteria gt(Object value) {
		return dialect().gt().invoke(this, value);
	}

	default SimpleCriteria ge(Object value) {
		return dialect().ge().invoke(this, value);
	}

	default SimpleCriteria between(Object min, Object max) { //included
		return dialect().between().invoke(this, min, max);
	}
	
	default SimpleCriteria like(Object value) {
		return dialect().like().invoke(this, value);
	}
	
	default SimpleCriteria startsLike(Object value) {
		return dialect().startsLike().invoke(this, value);
	}

	default SimpleCriteria endsLike(Object value) {
		return dialect().endsLike().invoke(this, value);
	}

	default SimpleCriteria contentLike(Object value) {
		return dialect().contentLike().invoke(this, value);
	}
	
	default SimpleCriteria startsNotLike(Object value) {
		return dialect().startsNotLike().invoke(this, value);
	}

	default SimpleCriteria endsNotLike(Object value) {
		return dialect().endsNotLike().invoke(this, value);
	}

	default SimpleCriteria contentNotLike(Object value) {
		return dialect().contentNotLike().invoke(this, value);
	}

	default SimpleCriteria notLike(Object value) {
		return dialect().notLike().invoke(this, value);
	}

	default SimpleCriteria ilike(Object value) {
		return dialect().iLike().invoke(this, value);
	}

	default SimpleCriteria notILike(Object value) {
		return dialect().notILike().invoke(this, value);
	}

	default SimpleCriteria isNull() {
		return dialect().isNull().invoke(this);
	}

	default SimpleCriteria notNull() {
		return dialect().notNull().invoke(this);
	}

	@SuppressWarnings("unchecked")
	default <T> SimpleCriteria in(T... arr) {
		return dialect().in().invoke(appendFirst(arr, this)); 
	}

	@SuppressWarnings("unchecked")
	default <T> SimpleCriteria notIn(T... arr) {
		return dialect().notIn().invoke(appendFirst(arr, this));
	}
	
	default SimpleCriteria filter(Predicate exp) {
		return new SimpleCriteria(this, exp);
	}
	
	// arithmetic operations
	
	default Column plus(Object o) {
		return dialect().plus().invoke(this, o);
	}

	default Column minus(Object o) {
		return dialect().minus().invoke(this, o);
	}

	default Column multiply(Object o) {
		return dialect().multiply().invoke(this, o);
	}

	default Column divide(Object o) {
		return dialect().divide().invoke(this, o);
	}
	
	//numeric functions
	
	default Column sqrt() {
		return dialect().sqrt().invoke(this);
	}
	
	default Column exp() {
		return dialect().exp().invoke(this);
	}
	
	default Column log() {
		return dialect().log().invoke(this);
	}

	default Column log(int base) {
		return dialect().log().invoke(this, base);
	}
	
	default Column abs() {
		return dialect().abs().invoke(this);
	}

	default Column ceil() {
		return dialect().ceil().invoke(this);
	}

	default Column floor() {
		return dialect().floor().invoke(this);
	}

	default Column trunc() {
		return dialect().trunc().invoke(this);
	}

	default Column trunc(int digit) {
		return dialect().trunc().invoke(this, digit);
	}
	
	default Column round() {
		return dialect().round().invoke(this);
	}
	
	default Column round(int digit) {
		return dialect().round().invoke(this, digit);
	}
	
	default Column mod(int value) {
		return dialect().mod().invoke(this, value);
	}
	
	default Column pow(int value) {
		return dialect().pow().invoke(this, value);
	}

	//bitwise functions
	
	default Column bitAnd(int value) {
		return dialect().bitAnd().invoke(this, value);
	}
	
	default Column bitOr(int value) {
		return dialect().bitOr().invoke(this, value);
	}
	
	default Column bitXor(int value) {
		return dialect().bitXor().invoke(this, value);
	}
	
	default Column bitNot(int value) {
		return dialect().bitNot().invoke(this, value);
	}
	
	default Column bitShiftLeft(int value) {
		return dialect().bitShiftLeft().invoke(this, value);
	}
	
	default Column bitShiftRight(int value) {
		return dialect().bitShiftRight().invoke(this, value);
	}
	
	//string functions

	default Column length() {
		return dialect().length().invoke(this);
	}
	
	default Column trim() {
		return dialect().trim().invoke(this);
	}

	default Column ltrim() {
		return dialect().ltrim().invoke(this);
	}

	default Column rtrim() {
		return dialect().rtrim().invoke(this);
	}
	
	default Column upper() {
		return dialect().upper().invoke(this);
	}

	default Column lower() {
		return dialect().lower().invoke(this);
	}
	
	default Column initcap() {
		return dialect().initcap().invoke(this);
	}
	
	default Column reverse() {
		return dialect().reverse().invoke(this);
	}
	
	default Column left(int n) {
		return dialect().left().invoke(this, n);
	}
	
	default Column right(int n) {
		return dialect().pow().invoke(this, n);
	}
	
	default Column replace(String oldValue, String newValue) {
		return dialect().replace().invoke(this, oldValue, newValue);
	}
	
	default Column substring(int start, int end) {
		return dialect().substring().invoke(this, start, end);
	}
	
	default Column concat(Object... str) {
		return dialect().concat().invoke(appendFirst(str, this));
	}
	
	default Column lpad(int n, String value) {
		return dialect().lpad().invoke(this, n, value);
	}
	
	default Column rpad(int n, String value) {
		return dialect().rpad().invoke(this, n, value);
	}

	//temporal functions
	
	default Column year() {
		return dialect().year().invoke(this);
	}
	
	default Column month() {
		return dialect().month().invoke(this);
	}

	default Column week() {
		return dialect().week().invoke(this);
	}
	
	default Column day() {
		return dialect().day().invoke(this);
	}
	
	default Column dow() {
		return dialect().dow().invoke(this);
	}
	
	default Column doy() {
		return dialect().doy().invoke(this);
	}

	default Column hour() {
		return dialect().hour().invoke(this);
	}

	default Column minute() {
		return dialect().minute().invoke(this);
	}
	
	default Column second() {
		return dialect().second().invoke(this);
	}
	
	default Column epoch() {
		return dialect().epoch().invoke(this);
	}
	
	default Column yearMonth() {
		return dialect().yearMonth().invoke(this);
	}
	
	default Column yearWeek() {
		return dialect().yearWeek().invoke(this);
	}
	
	default Column monthDay() {
		return dialect().monthDay().invoke(this);
	}
	
	default Column hourMinute() {
		return dialect().hourMinute().invoke(this);
	}
	
	//cast functions

	default Column varchar() {
		return dialect().varchar().invoke(this);
	}
	
	default Column varchar(int size) {
		return dialect().varchar().invoke(this, size);
	}
	
	default Column date() {
		return dialect().date().invoke(this);
	}
	
	default Column timestamp() {
		return dialect().timestamp().invoke(this);
	}
	
	default Column integer() {
		return dialect().integer().invoke(this);
	}
	
	default Column bigint() {
		return dialect().bigint().invoke(this);
	}
	
	default Column decimal() {
		return dialect().decimal().invoke(this);
	}
	
	default Column decimal(int digit, int precision) {
		return dialect().decimal().invoke(this, digit, precision);
	}

	//other functions
	
	default Column distinct() {
		return dialect().distinct().invoke(this);
	}
	
	default Column coalesce(Object o) {
		return dialect().coalesce().invoke(this, o);
	}
	
	//aggregate functions

	default Column count() {
		return dialect().count().invoke(this);
	}

	default Column min() {
		return dialect().min().invoke(this);
	}

	default Column max() {
		return dialect().max().invoke(this);
	}

	default Column sum() {
		return dialect().sum().invoke(this);
	}
	
	default Column avg() {
		return dialect().avg().invoke(this);
	}

	//pipe functions
	
	default Column over(Column[] cols, Order[] orders) {
		return over(new Partition(cols, orders));
	}
	
	default Column over(Partition part) {
		return dialect().over().invoke(this, part);
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
		return dialect().cdate().invoke();
	}
	
	static Column ctime() {
		return dialect().ctime().invoke();
	}
	
	static Column ctimestamp() {
		return dialect().ctimestamp().invoke();
	}

	static Column countAll(DBView... view) {
		return dialect().count().invoke(allColumns(view));
	}
	
	//window functions
	
	static Column rank() {
		return dialect().rank().invoke();
	}
	
	static Column rowNumber() {
		return dialect().rowNumber().invoke();
	}
	
	static Column denseRank() {
		return dialect().denseRank().invoke();
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

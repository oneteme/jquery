package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Dialect.getDialect;
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
	
	default Criteria eq(Object value) {
		return getDialect().eq().invoke(this, value);
	}

	default Criteria ne(Object value) {
		return getDialect().ne().invoke(this, value);
	}

	default Criteria lt(Object value) {
		return getDialect().lt().invoke(this, value);
	}

	default Criteria le(Object value) {
		return getDialect().le().invoke(this, value);
	}

	default Criteria gt(Object value) {
		return getDialect().gt().invoke(this, value);
	}

	default Criteria ge(Object value) {
		return getDialect().ge().invoke(this, value);
	}

	default Criteria between(Object min, Object max) { //included
		return getDialect().between().invoke(this, min, max);
	}
	
	default Criteria like(Object value) {
		return getDialect().like().invoke(this, value);
	}
	
	default Criteria startsLike(Object value) {
		return getDialect().startsLike().invoke(this, value);
	}

	default Criteria endsLike(Object value) {
		return getDialect().endsLike().invoke(this, value);
	}

	default Criteria contentLike(Object value) {
		return getDialect().contentLike().invoke(this, value);
	}
	
	default Criteria startsNotLike(Object value) {
		return getDialect().startsNotLike().invoke(this, value);
	}

	default Criteria endsNotLike(Object value) {
		return getDialect().endsNotLike().invoke(this, value);
	}

	default Criteria contentNotLike(Object value) {
		return getDialect().contentNotLike().invoke(this, value);
	}

	default Criteria notLike(Object value) {
		return getDialect().notLike().invoke(this, value);
	}

	default Criteria ilike(Object value) {
		return getDialect().iLike().invoke(this, value);
	}

	default Criteria notILike(Object value) {
		return getDialect().notILike().invoke(this, value);
	}

	default Criteria isNull() {
		return getDialect().isNull().invoke(this);
	}

	default Criteria notNull() {
		return getDialect().notNull().invoke(this);
	}

	@SuppressWarnings("unchecked")
	default <T> Criteria in(T... arr) {
		return getDialect().in().invoke(appendFirst(arr, this)); 
	}

	@SuppressWarnings("unchecked")
	default <T> Criteria notIn(T... arr) {
		return getDialect().notIn().invoke(appendFirst(arr, this));
	}
	
	default Criteria filter(Predicate exp) {
		return new SimpleCriteria(this, exp);
	}
	
	// arithmetic operations
	
	default Column plus(Object o) {
		return getDialect().plus().invoke(this, o);
	}

	default Column minus(Object o) {
		return getDialect().minus().invoke(this, o);
	}

	default Column multiply(Object o) {
		return getDialect().multiply().invoke(this, o);
	}

	default Column divide(Object o) {
		return getDialect().divide().invoke(this, o);
	}
	
	//numeric functions
	
	default Column sqrt() {
		return getDialect().sqrt().invoke(this);
	}
	
	default Column exp() {
		return getDialect().exp().invoke(this);
	}
	
	default Column log() {
		return getDialect().log().invoke(this);
	}

	default Column log(int base) {
		return getDialect().log().invoke(this, base);
	}
	
	default Column abs() {
		return getDialect().abs().invoke(this);
	}

	default Column ceil() {
		return getDialect().ceil().invoke(this);
	}

	default Column floor() {
		return getDialect().floor().invoke(this);
	}

	default Column trunc() {
		return getDialect().trunc().invoke(this);
	}

	default Column trunc(int digit) {
		return getDialect().trunc().invoke(this, digit);
	}
	
	default Column round() {
		return getDialect().round().invoke(this);
	}
	
	default Column round(int digit) {
		return getDialect().round().invoke(this, digit);
	}
	
	default Column mod(int value) {
		return getDialect().mod().invoke(this, value);
	}
	
	default Column pow(int value) {
		return getDialect().pow().invoke(this, value);
	}

	//bitwise functions
	
	default Column bitAnd(int value) {
		return getDialect().bitAnd().invoke(this, value);
	}
	
	default Column bitOr(int value) {
		return getDialect().bitOr().invoke(this, value);
	}
	
	default Column bitXor(int value) {
		return getDialect().bitXor().invoke(this, value);
	}
	
	default Column bitNot(int value) {
		return getDialect().bitNot().invoke(this, value);
	}
	
	default Column bitShiftLeft(int value) {
		return getDialect().bitShiftLeft().invoke(this, value);
	}
	
	default Column bitShiftRight(int value) {
		return getDialect().bitShiftRight().invoke(this, value);
	}
	
	//string functions

	default Column length() {
		return getDialect().length().invoke(this);
	}
	
	default Column trim() {
		return getDialect().trim().invoke(this);
	}

	default Column ltrim() {
		return getDialect().ltrim().invoke(this);
	}

	default Column rtrim() {
		return getDialect().rtrim().invoke(this);
	}
	
	default Column upper() {
		return getDialect().upper().invoke(this);
	}

	default Column lower() {
		return getDialect().lower().invoke(this);
	}
	
	default Column initcap() {
		return getDialect().initcap().invoke(this);
	}
	
	default Column reverse() {
		return getDialect().reverse().invoke(this);
	}
	
	default Column left(int n) {
		return getDialect().left().invoke(this, n);
	}
	
	default Column right(int n) {
		return getDialect().pow().invoke(this, n);
	}
	
	default Column replace(String oldValue, String newValue) {
		return getDialect().replace().invoke(this, oldValue, newValue);
	}
	
	default Column substring(int start, int end) {
		return getDialect().substring().invoke(this, start, end);
	}
	
	default Column concat(Object... str) {
		return getDialect().concat().invoke(appendFirst(str, this));
	}
	
	default Column lpad(int n, String value) {
		return getDialect().lpad().invoke(this, n, value);
	}
	
	default Column rpad(int n, String value) {
		return getDialect().rpad().invoke(this, n, value);
	}

	//temporal functions
	
	default Column year() {
		return getDialect().year().invoke(this);
	}
	
	default Column month() {
		return getDialect().month().invoke(this);
	}

	default Column week() {
		return getDialect().week().invoke(this);
	}
	
	default Column day() {
		return getDialect().day().invoke(this);
	}
	
	default Column dow() {
		return getDialect().dow().invoke(this);
	}
	
	default Column doy() {
		return getDialect().doy().invoke(this);
	}

	default Column hour() {
		return getDialect().hour().invoke(this);
	}

	default Column minute() {
		return getDialect().minute().invoke(this);
	}
	
	default Column second() {
		return getDialect().second().invoke(this);
	}
	
	default Column epoch() {
		return getDialect().epoch().invoke(this);
	}
	
	default Column yearMonth() {
		return getDialect().yearMonth().invoke(this);
	}
	
	default Column yearWeek() {
		return getDialect().yearWeek().invoke(this);
	}
	
	default Column monthDay() {
		return getDialect().monthDay().invoke(this);
	}
	
	default Column hourMinute() {
		return getDialect().hourMinute().invoke(this);
	}
	
	//cast functions

	default Column varchar() {
		return getDialect().varchar().invoke(this);
	}
	
	default Column varchar(int size) {
		return getDialect().varchar().invoke(this, size);
	}
	
	default Column date() {
		return getDialect().date().invoke(this);
	}
	
	default Column timestamp() {
		return getDialect().timestamp().invoke(this);
	}
	
	default Column integer() {
		return getDialect().integer().invoke(this);
	}
	
	default Column bigint() {
		return getDialect().bigint().invoke(this);
	}
	
	default Column decimal() {
		return getDialect().decimal().invoke(this);
	}
	
	default Column decimal(int digit, int precision) {
		return getDialect().decimal().invoke(this, digit, precision);
	}

	//other functions
	
	default Column distinct() {
		return getDialect().distinct().invoke(this);
	}
	
	default Column coalesce(Object o) {
		return getDialect().coalesce().invoke(this, o);
	}
	
	//aggregate functions

	default Column count() {
		return getDialect().count().invoke(this);
	}

	default Column min() {
		return getDialect().min().invoke(this);
	}

	default Column max() {
		return getDialect().max().invoke(this);
	}

	default Column sum() {
		return getDialect().sum().invoke(this);
	}
	
	default Column avg() {
		return getDialect().avg().invoke(this);
	}

	//pipe functions
	
	default Column over(Column[] cols, Order[] orders) {
		return over(new Partition(cols, orders));
	}
	
	default Column over(Partition part) {
		return getDialect().over().invoke(this, part);
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
	
	default CaseColumnComposer toCase() {
		return new CaseColumnComposer(this);
	}
	
	static CaseColumnComposer beginCase() {
		return new CaseColumnComposer(); //no column, append filter only
	}
	
	// constants
	
	static Column cdate() {
		return getDialect().cdate().invoke();
	}
	
	static Column ctime() {
		return getDialect().ctime().invoke();
	}
	
	static Column ctimestamp() {
		return getDialect().ctimestamp().invoke();
	}

	static Column countAll(DBView... view) {
		return getDialect().count().invoke(allColumns(view));
	}
	
	//window functions
	
	static Column rank() {
		return getDialect().rank().invoke();
	}
	
	static Column rowNumber() {
		return getDialect().rowNumber().invoke();
	}
	
	static Column denseRank() {
		return getDialect().denseRank().invoke();
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

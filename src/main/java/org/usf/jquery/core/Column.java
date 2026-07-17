package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.OrderType.ASC;
import static org.usf.jquery.core.OrderType.DESC;
import static org.usf.jquery.core.Stores.getCurrentDialect;
import static org.usf.jquery.core.Utils.appendFirst;
import static org.usf.jquery.core.Utils.toList;
import static org.usf.jquery.core.Validation.requireLegalVariable;
import static org.usf.jquery.core.Validation.requireNoArgs;

import java.util.Objects;

import org.usf.jquery.core.JavaType.Typed;

/**
 * 
 * @author u$f
 *
 */
public interface Column extends QueryPart, Typed {
	
	void build(SqlBuilder builder);
	
	@Override
	default void build(SqlBuilder builder, Object... args) {
		requireNoArgs(args, Column.class::getSimpleName);
		var ref = builder.aliasFor(this);
		if(nonNull(ref)) {
			builder.append(builder.getStore().dialect().suroundColumnAlias(ref));
		}
		else {
			build(builder);
		}
	}
	
	default String getTag() {
		return null;
	}
	
	default Column as(JDBCType type) {
		return as(getTag(), getType());
	}
	
	default Column as(String alias) {
		return as(alias, getType());
	}
	
	default Column as(String alias, JDBCType type) {
		if(type != getType() || !Objects.equals(alias, getTag())) {
			return new ColumnProxy(this, type, nonNull(alias) ? requireLegalVariable(alias) : null);
		}
		return this;
	}
	
	//criteria
	
	default Criteria eq(Object value) {
		return getCurrentDialect().eq().invoke(this, value);
	}

	default Criteria ne(Object value) {
		return getCurrentDialect().ne().invoke(this, value);
	}

	default Criteria lt(Object value) {
		return getCurrentDialect().lt().invoke(this, value);
	}

	default Criteria le(Object value) {
		return getCurrentDialect().le().invoke(this, value);
	}

	default Criteria gt(Object value) {
		return getCurrentDialect().gt().invoke(this, value);
	}

	default Criteria ge(Object value) {
		return getCurrentDialect().ge().invoke(this, value);
	}

	default Criteria between(Object min, Object max) { //included
		return getCurrentDialect().between().invoke(this, min, max);
	}
	
	default Criteria like(Object value) {
		return getCurrentDialect().like().invoke(this, value);
	}
	
	default Criteria startsLike(Object value) {
		return getCurrentDialect().startsLike().invoke(this, value);
	}

	default Criteria endsLike(Object value) {
		return getCurrentDialect().endsLike().invoke(this, value);
	}

	default Criteria contentLike(Object value) {
		return getCurrentDialect().contentLike().invoke(this, value);
	}
	
	default Criteria startsNotLike(Object value) {
		return getCurrentDialect().startsNotLike().invoke(this, value);
	}

	default Criteria endsNotLike(Object value) {
		return getCurrentDialect().endsNotLike().invoke(this, value);
	}

	default Criteria contentNotLike(Object value) {
		return getCurrentDialect().contentNotLike().invoke(this, value);
	}

	default Criteria notLike(Object value) {
		return getCurrentDialect().notLike().invoke(this, value);
	}

	default Criteria ilike(Object value) {
		return getCurrentDialect().iLike().invoke(this, value);
	}

	default Criteria notILike(Object value) {
		return getCurrentDialect().notILike().invoke(this, value);
	}

	default Criteria isNull() {
		return getCurrentDialect().isNull().invoke(this);
	}

	default Criteria notNull() {
		return getCurrentDialect().notNull().invoke(this);
	}

	@SuppressWarnings("unchecked")
	default <T> Criteria in(T... arr) {
		return getCurrentDialect().in().invoke(appendFirst(arr, this)); 
	}

	@SuppressWarnings("unchecked")
	default <T> Criteria notIn(T... arr) {
		return getCurrentDialect().notIn().invoke(appendFirst(arr, this));
	}
	
	default Criteria filter(Predicate exp) {
		return new SimpleCriteria(this, exp);
	}
	
	// arithmetic operations
	
	default Column plus(Object o) {
		return getCurrentDialect().plus().invoke(this, o);
	}

	default Column minus(Object o) {
		return getCurrentDialect().minus().invoke(this, o);
	}

	default Column multiply(Object o) {
		return getCurrentDialect().multiply().invoke(this, o);
	}

	default Column divide(Object o) {
		return getCurrentDialect().divide().invoke(this, o);
	}
	
	//numeric functions
	
	default Column sqrt() {
		return getCurrentDialect().sqrt().invoke(this);
	}
	
	default Column exp() {
		return getCurrentDialect().exp().invoke(this);
	}
	
	default Column log() {
		return getCurrentDialect().log().invoke(this);
	}

	default Column log(int base) {
		return getCurrentDialect().log().invoke(this, base);
	}
	
	default Column abs() {
		return getCurrentDialect().abs().invoke(this);
	}

	default Column ceil() {
		return getCurrentDialect().ceil().invoke(this);
	}

	default Column floor() {
		return getCurrentDialect().floor().invoke(this);
	}

	default Column trunc() {
		return getCurrentDialect().trunc().invoke(this);
	}

	default Column trunc(int digit) {
		return getCurrentDialect().trunc().invoke(this, digit);
	}
	
	default Column round() {
		return getCurrentDialect().round().invoke(this);
	}
	
	default Column round(int digit) {
		return getCurrentDialect().round().invoke(this, digit);
	}
	
	default Column mod(int value) {
		return getCurrentDialect().mod().invoke(this, value);
	}
	
	default Column pow(int value) {
		return getCurrentDialect().pow().invoke(this, value);
	}

	//bitwise functions
	
	default Column bitAnd(int value) {
		return getCurrentDialect().bitAnd().invoke(this, value);
	}
	
	default Column bitOr(int value) {
		return getCurrentDialect().bitOr().invoke(this, value);
	}
	
	default Column bitXor(int value) {
		return getCurrentDialect().bitXor().invoke(this, value);
	}
	
	default Column bitNot() {
		return getCurrentDialect().bitNot().invoke(this);
	}
	
	default Column bitShiftLeft(int value) {
		return getCurrentDialect().bitShiftLeft().invoke(this, value);
	}
	
	default Column bitShiftRight(int value) {
		return getCurrentDialect().bitShiftRight().invoke(this, value);
	}
	
	//string functions

	default Column length() {
		return getCurrentDialect().length().invoke(this);
	}
	
	default Column trim() {
		return getCurrentDialect().trim().invoke(this);
	}

	default Column ltrim() {
		return getCurrentDialect().ltrim().invoke(this);
	}

	default Column rtrim() {
		return getCurrentDialect().rtrim().invoke(this);
	}
	
	default Column upper() {
		return getCurrentDialect().upper().invoke(this);
	}

	default Column lower() {
		return getCurrentDialect().lower().invoke(this);
	}
	
	default Column initcap() {
		return getCurrentDialect().initcap().invoke(this);
	}
	
	default Column reverse() {
		return getCurrentDialect().reverse().invoke(this);
	}
	
	default Column left(int n) {
		return getCurrentDialect().left().invoke(this, n);
	}
	
	default Column right(int n) {
		return getCurrentDialect().pow().invoke(this, n);
	}
	
	default Column replace(String oldValue, String newValue) {
		return getCurrentDialect().replace().invoke(this, oldValue, newValue);
	}
	
	default Column substring(int start, int end) {
		return getCurrentDialect().substring().invoke(this, start, end);
	}
	
	default Column concat(Object... str) {
		return getCurrentDialect().concat().invoke(appendFirst(str, this));
	}
	
	default Column lpad(int n, String value) {
		return getCurrentDialect().lpad().invoke(this, n, value);
	}
	
	default Column rpad(int n, String value) {
		return getCurrentDialect().rpad().invoke(this, n, value);
	}

	//temporal functions
	
	default Column year() {
		return getCurrentDialect().year().invoke(this);
	}
	
	default Column month() {
		return getCurrentDialect().month().invoke(this);
	}

	default Column week() {
		return getCurrentDialect().week().invoke(this);
	}
	
	default Column day() {
		return getCurrentDialect().day().invoke(this);
	}
	
	default Column dow() {
		return getCurrentDialect().dow().invoke(this);
	}
	
	default Column doy() {
		return getCurrentDialect().doy().invoke(this);
	}

	default Column hour() {
		return getCurrentDialect().hour().invoke(this);
	}

	default Column minute() {
		return getCurrentDialect().minute().invoke(this);
	}
	
	default Column second() {
		return getCurrentDialect().second().invoke(this);
	}
	
	default Column epoch() {
		return getCurrentDialect().epoch().invoke(this);
	}
	
	default Column yearMonth() {
		return getCurrentDialect().yearMonth().invoke(this);
	}
	
	default Column yearWeek() {
		return getCurrentDialect().yearWeek().invoke(this);
	}
	
	default Column monthDay() {
		return getCurrentDialect().monthDay().invoke(this);
	}
	
	default Column hourMinute() {
		return getCurrentDialect().hourMinute().invoke(this);
	}
	
	//cast functions

	default Column varchar() {
		return getCurrentDialect().varchar().invoke(this);
	}
	
	default Column varchar(int size) {
		return getCurrentDialect().varchar().invoke(this, size);
	}
	
	default Column date() {
		return getCurrentDialect().date().invoke(this);
	}
	
	default Column timestamp() {
		return getCurrentDialect().timestamp().invoke(this);
	}
	
	default Column integer() {
		return getCurrentDialect().integer().invoke(this);
	}
	
	default Column bigint() {
		return getCurrentDialect().bigint().invoke(this);
	}
	
	default Column decimal() {
		return getCurrentDialect().decimal().invoke(this);
	}
	
	default Column decimal(int digit, int precision) {
		return getCurrentDialect().decimal().invoke(this, digit, precision);
	}

	//other functions
	
	default Column distinct() {
		return getCurrentDialect().distinct().invoke(this);
	}
	
	default Column coalesce(Object o) {
		return getCurrentDialect().coalesce().invoke(this, o);
	}
	
	//aggregate functions

	default Column count() {
		return getCurrentDialect().count().invoke(this);
	}

	default Column min() {
		return getCurrentDialect().min().invoke(this);
	}

	default Column max() {
		return getCurrentDialect().max().invoke(this);
	}

	default Column sum() {
		return getCurrentDialect().sum().invoke(this);
	}
	
	default Column avg() {
		return getCurrentDialect().avg().invoke(this);
	}

	//scope functions
	
	default Column over(Column[] cols, Order[] orders) {
		return over(new Partition(toList(cols), toList(orders)));
	}
	
	default Column over(Partition part) {
		return getCurrentDialect().over().invoke(this, part);
	}
	
	default Column within(Order... orders) {
		return within(new Group(toList(orders)));
	}
	
	default Column within(Group group) {
		return getCurrentDialect().within().invoke(this, group);
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
	
	static Column pi() {
		return getCurrentDialect().pi().invoke();
	}
	
	static Column cdate() {
		return getCurrentDialect().cdate().invoke();
	}
	
	static Column ctime() {
		return getCurrentDialect().ctime().invoke();
	}
	
	static Column ctimestamp() {
		return getCurrentDialect().ctimestamp().invoke();
	}

	static Column countAll(View... view) {
		return getCurrentDialect().count().invoke(allColumns(view));
	}
	
	//window functions
	
	static Column rank() {
		return getCurrentDialect().rank().invoke();
	}
	
	static Column rowNumber() {
		return getCurrentDialect().rowNumber().invoke();
	}
	
	static Column denseRank() {
		return getCurrentDialect().denseRank().invoke();
	}
	
	static ViewColumn column(String value) {
		return new ViewColumn(value, null, null, null);
	}

	static ViewColumn column(String value, View view) {
		return new ViewColumn(value, view, null, null);
	}

	static ViewColumn column(String value, View view, JDBCType type) {
		return new ViewColumn(value, view, type, null);
	}

	static ViewColumn column(String value, JDBCType type) {
		return new ViewColumn(value, null, type, null);
	}
	
	static ViewColumn column(String value, View view, JDBCType type, String tag) {
		return new ViewColumn(value, view, type, tag);
	}

	static AsteriskColumn allColumns(View... views) {
		return new AsteriskColumn(nonNull(views) ? toList(views) : null);
	}
	
	static ValueColumn constant(Object value) {
		return constant(value, JDBCType.typeOf(value).orElse(null));
	}

	static ValueColumn constant(Object value, JDBCType type) {
		return new ValueColumn(value, type);
	}
}

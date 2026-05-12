package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.JDBCType.DATE;
import static org.usf.jquery.core.JDBCType.INTEGER;
import static org.usf.jquery.core.JDBCType.TIME;
import static org.usf.jquery.core.JDBCType.TIMESTAMP;
import static org.usf.jquery.core.JDBCType.TIMESTAMP_WITH_TIMEZONE;
import static org.usf.jquery.core.JDBCType.VARCHAR;
import static org.usf.jquery.core.Parameter.required;
import static org.usf.jquery.core.Provider.DEFAULT;
import static org.usf.jquery.core.Stores.getCurrentStore;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 * 
 */
@Getter
@RequiredArgsConstructor
public class Dialect implements Composers, Operators, Comparators {
	
	private static final Dialect DEFAULT_DIALECT = new Dialect(DEFAULT);
	
	private final Provider provider;

	//allow specific query composer for this dialect
	public QueryComposer newQueryComposer(){
		return new QueryComposer();
	}

	//allow specific query building for this dialect
	public QueryView newQueryView(){
		return new QueryView();
	}
	
	//combined functions
	
	public MacroDefinition semester() {//[1-2]
		return new MacroDefinition("semester", INTEGER, 
				args-> month().invoke(args).toCase()
				.when(Predicate.lt(7), 1)
				.orElse(2), 
				required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}
	
	public MacroDefinition quarter() {//[1-4]
		return new MacroDefinition("quarter", INTEGER, 
				args-> month().invoke(args).toCase()
				.when(Predicate.lt(4), 1)
				.when(Predicate.lt(7), 2)
				.when(Predicate.lt(10), 3)
				.orElse(4), 
				required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}
	
	public MacroDefinition yearSemester() {//YYYY-'S'S
		var varchar = varchar();
		return new MacroDefinition("yearSemester", VARCHAR,  
				args-> concat().invoke(
				varchar.invoke(year().invoke(args)),
				"-S",
				varchar.invoke(semester().invoke(args))), 
				required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}
	
	public MacroDefinition yearQuarter() {//YYYY-'Q'Q
		var varchar = varchar();
		return new MacroDefinition("yearQuarter", VARCHAR, 
				args-> concat().invoke(
				varchar.invoke(year().invoke(args)),
				"-Q",
				varchar.invoke(quarter().invoke(args))), 
				required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}
	
	public MacroDefinition yearWeek() {//YYYY-'W'WW
		var varchar = varchar();
		return new MacroDefinition("yearWeek", VARCHAR, 
				args-> concat().invoke(
				varchar.invoke(year().invoke(args[0])), 
				"-W", 
				lpad().invoke(varchar.invoke(doy().invoke(args[0])), 2, "0")), 
				required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	public MacroDefinition yearMonth() {//YYYY-MM
		return new MacroDefinition("yearMonth", VARCHAR, 
				args-> left().invoke(varchar().invoke(args), 7), 
				required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}
	
	public MacroDefinition monthDay() {//MM-DD
		return new MacroDefinition("monthDay", VARCHAR, 
				args-> substring().invoke(varchar().invoke(args[0]), 6, 5), 
				required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	public MacroDefinition hourMinute() {//HH:MM
		return new MacroDefinition("hourMinute", VARCHAR, 
				args-> {
					var time = JDBCType.typeOf(args[0])
							.filter(t-> t == TIME)
							.map(t-> args[0])
							.orElseGet(()-> time().invoke(args[0]));
					return left().invoke(varchar().invoke(time), 5);
					}, required(TIME, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	public boolean supportGroupByIndex() {
		return true;
	}

	public boolean supportGroupByAlias() {
		return true;
	}

	public boolean supportHavingByAlias() {
		return true;
	}
	
	public boolean supportFetchClause() { //ORACLE
		return false;
	}
	
	public boolean supportTopClause() { //TERADATA & SQL SERVER
		return false;
	}
	
	public boolean supportLimitClause() { //PG & MYSQL
		return true;
	}
	
	public boolean supportOffsetClause() { //PG & MYSQL
		return true;
	}
	
	public static Dialect getDialect() {		
		var store = getCurrentStore();
		return nonNull(store) ? store.dialect() : DEFAULT_DIALECT;
	}
}

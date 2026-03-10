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
public class Dialect implements Syntaxes, Operators, Comparators {
	
	private static final Dialect DEFAULT_DIALECT = new Dialect(DEFAULT);
	
	private final Provider provider;
	
	//allow specific query building for this dialect
	public Query buildQuery(QueryView view){
		return view.build();
	}
	
	//combined functions
	
	public Definition<Column> semester() {//[1-2]
		Macro op = args-> month().invoke(args).toCase()
				.when(Predicate.lt(7), 1)
				.orElse(2);
		return new Definition<>("semester", INTEGER, op, required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}
	
	public Definition<Column> quarter() {//[1-4]
		Macro mcr = args-> month().invoke(args).toCase()
				.when(Predicate.lt(4), 1)
				.when(Predicate.lt(7), 2)
				.when(Predicate.lt(10), 3)
				.orElse(4);
		return new Definition<>("quarter", INTEGER, mcr, required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}
	
	public Definition<Column> yearSemester() {//YYYY-'S'S
		var varchar = varchar();
		Macro mcr = args-> concat().invoke(
				varchar.invoke(year().invoke(args)),
				"-S",
				varchar.invoke(semester().invoke(args)));
		return new Definition<>("yearSemester", VARCHAR, mcr, required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}
	
	public Definition<Column> yearQuarter() {//YYYY-'Q'Q
		var varchar = varchar();
		Macro mcr = args-> concat().invoke(
				varchar.invoke(year().invoke(args)),
				"-Q",
				varchar.invoke(quarter().invoke(args)));
		return new Definition<>("yearQuarter", VARCHAR, mcr, required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}
	
	public Definition<Column> yearWeek() {//YYYY-'W'WW
		var varchar = varchar();
		Macro op = args-> concat().invoke(
				varchar.invoke(year().invoke(args[0])), 
				"-W", 
				lpad().invoke(varchar.invoke(doy().invoke(args[0])), 2, "0"));
		return new Definition<>("yearWeek", VARCHAR, op, required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	public Definition<Column> yearMonth() {//YYYY-MM
		Macro mcr = args-> left().invoke(varchar().invoke(args), 7);
		return new Definition<>("yearMonth", VARCHAR, mcr, required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}
	
	public Definition<Column> monthDay() {//MM-DD
		Macro mcr = args-> substring().invoke(varchar().invoke(args[0]), 6, 5);
		return new Definition<>("monthDay", VARCHAR, mcr, required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	public Definition<Column> hourMinute() {//HH:MM
		Macro mcr = args-> {
			var time = JDBCType.typeOf(args[0])
					.filter(t-> t == TIME)
					.map(t-> args[0])
					.orElseGet(()-> time().invoke(args[0]));
			return left().invoke(varchar().invoke(time), 5);
		};
		return new Definition<>("hourMinute", VARCHAR, mcr, required(TIME, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	public static Dialect getDialect() {		
		var store = getCurrentStore();
		return nonNull(store) ? store.dialect() : DEFAULT_DIALECT;
	}
}

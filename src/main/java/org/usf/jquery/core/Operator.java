package org.usf.jquery.core;

import static org.usf.jquery.core.ArgTypeRef.firstArgJdbcType;
import static org.usf.jquery.core.DBProcessor.lookup;
import static org.usf.jquery.core.Database.TERADATA;
import static org.usf.jquery.core.Database.currentDatabase;
import static org.usf.jquery.core.JDBCType.BIGINT;
import static org.usf.jquery.core.JDBCType.DATE;
import static org.usf.jquery.core.JDBCType.DOUBLE;
import static org.usf.jquery.core.JDBCType.INTEGER;
import static org.usf.jquery.core.JDBCType.TIME;
import static org.usf.jquery.core.JDBCType.TIMESTAMP;
import static org.usf.jquery.core.JDBCType.TIMESTAMP_WITH_TIMEZONE;
import static org.usf.jquery.core.JDBCType.VARCHAR;
import static org.usf.jquery.core.JQueryType.COLUMN;
import static org.usf.jquery.core.JQueryType.PARTITION;
import static org.usf.jquery.core.Parameter.optional;
import static org.usf.jquery.core.Parameter.required;
import static org.usf.jquery.core.Parameter.varargs;
import static org.usf.jquery.core.Validation.requireNArgs;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * 
 * @author u$f
 *
 */
public interface Operator extends DBProcessor {
	
	String id(); //nullable

	default OperationColumn operation(JDBCType type, Object... args) {
		return new OperationColumn(this, args, type);
	}
	
	default boolean is(Class<? extends Operator> type) {
		return type.isInstance(this);
	}
	
	default boolean is(String name) {
		return name.equals(id());
	}
	
	//Arithmetic operations

	static TypedOperator plus() {
		return new TypedOperator(firstArgJdbcType(), operator("+"), required(), required(firstArgJdbcType()));
	}

	static TypedOperator minus() {
		return new TypedOperator(firstArgJdbcType(), operator("-"), required(), required(firstArgJdbcType())); //date|datetime
	}

	static TypedOperator multiply() {
		return new TypedOperator(firstArgJdbcType(), operator("*"), required(), required(firstArgJdbcType()));
	}
	
	static TypedOperator divide() {
		return new TypedOperator(firstArgJdbcType(), operator("/"), required(), required(firstArgJdbcType()));
	}
	
	//numeric functions
	
	static TypedOperator sqrt() {
		return new TypedOperator(DOUBLE, function("SQRT"), required(DOUBLE)); 
	}
	
	static TypedOperator exp() {
		return new TypedOperator(DOUBLE, function("EXP"), required(DOUBLE)); 
	}
	
	static TypedOperator log() {
		return new TypedOperator(DOUBLE, function("LOG"), required(DOUBLE), optional(INTEGER)); 
	}
	
	static TypedOperator abs() {
		return new TypedOperator(DOUBLE, function("ABS"), required(DOUBLE));
	}

	static TypedOperator ceil() {
		return new TypedOperator(BIGINT, function("CEIL"), required(DOUBLE)); 
	}

	static TypedOperator floor() {
		return new TypedOperator(BIGINT, function("FLOOR"), required(DOUBLE)); 
	}

	static TypedOperator trunc() {
		return new TypedOperator(BIGINT, function("TRUNC"), required(DOUBLE), optional(INTEGER)); 
	}
	
	static TypedOperator round() {
		return new TypedOperator(DOUBLE, function("ROUND"), required(DOUBLE), optional(INTEGER));
	}
	
	static TypedOperator mod() {
		return new TypedOperator(BIGINT, function("MOD"), required(DOUBLE), required(DOUBLE));
	}	
	
	static TypedOperator pow() {
		return new TypedOperator(DOUBLE, function("POW"), required(DOUBLE), required(DOUBLE));
	}
	
	//string functions

	static TypedOperator length() {
		return new TypedOperator(INTEGER, function("LENGTH"), required(VARCHAR));
	}
	
	static TypedOperator trim() {
		return new TypedOperator(VARCHAR, function("TRIM"), required(VARCHAR));
	}

	static TypedOperator ltrim() {
		return new TypedOperator(VARCHAR, function("LTRIM"), required(VARCHAR));
	}

	static TypedOperator rtrim() {
		return new TypedOperator(VARCHAR, function("RTRIM"), required(VARCHAR));
	}
	
	static TypedOperator upper() {
		return new TypedOperator(VARCHAR, function("UPPER"), required(VARCHAR));
	}

	static TypedOperator lower() {
		return new TypedOperator(VARCHAR, function("LOWER"), required(VARCHAR));
	}
	
	static TypedOperator initcap() {
		return new TypedOperator(VARCHAR, function("INITCAP"), required(VARCHAR));
	}
	
	static TypedOperator reverse() {
		return new TypedOperator(VARCHAR, function("REVERSE"), required(VARCHAR));
	}
	
	static TypedOperator left() {
		return new TypedOperator(VARCHAR, function("LEFT"), required(VARCHAR), required(INTEGER));
	}
	
	static TypedOperator right() {
		return new TypedOperator(VARCHAR, function("RIGHT"), required(VARCHAR), required(INTEGER));
	}
	
	static TypedOperator replace() {
		var id = currentDatabase() == TERADATA ? "OREPLACE" : "REPLACE";
		return new TypedOperator(VARCHAR, function(id), required(VARCHAR), required(VARCHAR), required(VARCHAR)); //!teradata
	}
	
	static TypedOperator substring() { //int start, int length
		return new TypedOperator(VARCHAR, function("SUBSTRING"), required(VARCHAR), required(INTEGER), required(INTEGER));
	}
	
	static TypedOperator concat() {
		return new TypedOperator(VARCHAR, function("CONCAT"), required(VARCHAR), required(VARCHAR), varargs(VARCHAR));
	}
	
	static TypedOperator lpad() {
		return new TypedOperator(VARCHAR, function("LPAD"), required(BIGINT, VARCHAR), required(INTEGER), required(VARCHAR));
	}
	
	static TypedOperator rpad() {
		return new TypedOperator(VARCHAR, function("RPAD"), required(BIGINT, VARCHAR), required(INTEGER), required(VARCHAR));
	}

	//temporal functions
	
	static TypedOperator year() {
		return new TypedOperator(INTEGER, extract("YEAR"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	static TypedOperator month() {
		return new TypedOperator(INTEGER, extract("MONTH"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}

	static TypedOperator week() {
		var fn  = currentDatabase() == TERADATA ? function("td_week_of_year") : extract("WEEK");//teradata 1st week index = 0
		return new TypedOperator(INTEGER, fn, required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}
	
	static TypedOperator day() {
		return new TypedOperator(INTEGER, extract("DAY"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	static TypedOperator dow() {
		var fn  = currentDatabase() == TERADATA ? function("td_day_of_week") : extract("DOW");
		return new TypedOperator(INTEGER, fn, required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	static TypedOperator doy() {
		var fn  = currentDatabase() == TERADATA ? function("td_day_of_year") : extract("DOY");
		return new TypedOperator(INTEGER, fn, required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}

	static TypedOperator hour() {
		return new TypedOperator(INTEGER, extract("HOUR"), required(TIME, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}

	static TypedOperator minute() {
		return new TypedOperator(INTEGER, extract("MINUTE"), required(TIME, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	static TypedOperator second() {
		return new TypedOperator(INTEGER, extract("SECOND"), required(TIME, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	static TypedOperator epoch() {
		return new TypedOperator(BIGINT, extract("EPOCH"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); //!Teradata
	}
	
	//combined functions
	
	static TypedOperator yearMonth() {//YYYY-MM
		CombinedOperator op = args-> left().operation(varchar().operation(requireNArgs(1, args, ()-> "yearMonth")[0]), 7);
		return new TypedOperator(VARCHAR, op, required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}
	
	static TypedOperator yearWeek() {//YYYY-'W'WW
		CombinedOperator op = args-> {
			var col = requireNArgs(1, args, ()-> "yearWeek")[0];
			return concat().operation(varchar().operation(year().operation(col)), 
					"-W", 
					lpad().operation(varchar().operation(doy().operation(col)), 2, "0"));
		};
		return new TypedOperator(VARCHAR, op, required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	static TypedOperator monthDay() {//MM-DD
		CombinedOperator op = args-> {
			var col = requireNArgs(1, args, ()-> "monthDay")[0];
			return substring().operation(varchar().operation(col), 6, 5);
		};
		return new TypedOperator(VARCHAR, op, required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	static TypedOperator hourMinute() {//HH:MM
		CombinedOperator op = args-> {
			var col = requireNArgs(1, args, ()-> "hourMinute")[0];
			var tim = JDBCType.typeOf(col).filter(t-> t == TIME)
					.map(t-> col).orElseGet(()-> time().operation(col));
			return left().operation(varchar().operation(tim), 5);
		};
		return new TypedOperator(VARCHAR, op, required(TIME, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	//cast functions

	static TypedOperator varchar() {
		return new TypedOperator(VARCHAR, cast("VARCHAR"), required(), optional(INTEGER)); //any
	}
	
	static TypedOperator date() {
		return new TypedOperator(DATE, cast("DATE"), required(VARCHAR, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}

	static TypedOperator time() {
		return new TypedOperator(TIME, cast("TIME"), required(TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	static TypedOperator timestamp() {
		return new TypedOperator(TIMESTAMP, cast("TIMESTAMP"), required(VARCHAR, DATE)); 
	}
	
	static TypedOperator integer() {
		return new TypedOperator(INTEGER, cast("INTEGER"), required(VARCHAR, DOUBLE));
	}
	
	static TypedOperator bigint() {
		return new TypedOperator(BIGINT, cast("BIGINT"), required(VARCHAR, DOUBLE)); // any number
	}
	
	static TypedOperator decimal() {
		return new TypedOperator(DOUBLE, cast("DECIMAL"), required(VARCHAR, BIGINT), optional(INTEGER), optional(INTEGER));
	}

	//other functions
	
	static TypedOperator coalesce() {
		return new TypedOperator(firstArgJdbcType(), function("COALESCE"), required(), required());
	}

	//aggregate functions

	static TypedOperator count() {
		return new TypedOperator(BIGINT, aggregation("COUNT"), required()); 
	}
	
	static TypedOperator min() {
		return new TypedOperator(firstArgJdbcType(), aggregation("MIN"), required()); 
	}

	static TypedOperator max() {
		return new TypedOperator(firstArgJdbcType(), aggregation("MAX"), required()); 
	}

	static TypedOperator sum() {
		return new TypedOperator(DOUBLE, aggregation("SUM"), required(DOUBLE));
	}
	
	static TypedOperator avg() {
		return new TypedOperator(DOUBLE, aggregation("AVG"), required(DOUBLE));
	}
	
	//window functions
	
	static TypedOperator rank() {
		return new TypedOperator(INTEGER, window("RANK")); // takes no args
	}
	
	static TypedOperator rowNumber() {
		return new TypedOperator(INTEGER, window("ROW_NUMBER")); // takes no args
	}
	
	static TypedOperator denseRank() {
		return new TypedOperator(INTEGER, window("DENSE_RANK")); // takes no args
	}

	//pipe functions
	
	static TypedOperator over() {
		return new TypedOperator(firstArgJdbcType(), pipe("OVER"), required(COLUMN), required(PARTITION)); //optional !?
	}

	// constant operators
	
	static TypedOperator cdate() {
		return new TypedOperator(DATE, constant("CURRENT_DATE"));
	}
	
	static TypedOperator ctime() {
		return new TypedOperator(TIME, constant("CURRENT_TIME"));
	}
	
	static TypedOperator ctimestamp() {
		return new TypedOperator(TIMESTAMP_WITH_TIMEZONE, constant("CURRENT_TIMESTAMP"));
	}

	static ArithmeticOperator operator(String symbol) {
		return ()-> symbol;
	}

	static FunctionOperator function(String name) {
		return ()-> name;
	}
	
	static ExtractFunction extract(String field) {
		return ()-> field;
	}

	static CastFunction cast(String type) {
		return ()-> type;
	}
	
	static WindowFunction window(String name) {
		return ()-> name;
	}
	
	static AggregateFunction aggregation(String name) {
		return ()-> name;
	}

	static PipeFunction pipe(String name) {
		return ()-> name;
	}

	static ConstantOperator constant(String name) {
		return ()-> name;
	}

	static Optional<TypedOperator> lookupOperator(String op) {
		return lookup(Operator.class, TypedOperator.class, op, null);
	}

	static Optional<TypedOperator> lookupOperator(String op, Predicate<TypedOperator> pre) {
		return lookup(Operator.class, TypedOperator.class, op, pre);
	}
}

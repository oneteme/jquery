package org.usf.jquery.core;

import static org.usf.jquery.core.ArgTypeRef.firstArgJdbcType;
import static org.usf.jquery.core.JDBCType.BIGINT;
import static org.usf.jquery.core.JDBCType.BOOLEAN;
import static org.usf.jquery.core.JDBCType.DATE;
import static org.usf.jquery.core.JDBCType.DOUBLE;
import static org.usf.jquery.core.JDBCType.INTEGER;
import static org.usf.jquery.core.JDBCType.TIME;
import static org.usf.jquery.core.JDBCType.TIMESTAMP;
import static org.usf.jquery.core.JDBCType.TIMESTAMP_WITH_TIMEZONE;
import static org.usf.jquery.core.JDBCType.VARCHAR;
import static org.usf.jquery.core.JQueryType.PARTITION;
import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.LogicalOperator.OR;
import static org.usf.jquery.core.Parameter.optional;
import static org.usf.jquery.core.Parameter.required;
import static org.usf.jquery.core.Parameter.varargs;
import static org.usf.jquery.core.Predicate.lt;
import static org.usf.jquery.core.Validation.requireNArgs;

/**
 * 
 * @author u$f
 *
 */
public interface Operators {
	
	//Arithmetic operations

	default TypedOperator plus() {
		return new TypedOperator(firstArgJdbcType(), operator("+"), required(), required(firstArgJdbcType()));
	}

	default TypedOperator minus() {
		return new TypedOperator(firstArgJdbcType(), operator("-"), required(), required(firstArgJdbcType())); //date|datetime
	}

	default TypedOperator multiply() {
		return new TypedOperator(firstArgJdbcType(), operator("*"), required(), required(firstArgJdbcType()));
	}
	
	default TypedOperator divide() {
		return new TypedOperator(firstArgJdbcType(), operator("/"), required(), required(firstArgJdbcType()));
	}
	
	//numeric functions
	
	default TypedOperator sqrt() {
		return new TypedOperator(DOUBLE, function("SQRT"), required(DOUBLE)); 
	}
	
	default TypedOperator exp() {
		return new TypedOperator(DOUBLE, function("EXP"), required(DOUBLE)); 
	}
	
	default TypedOperator log() {
		return new TypedOperator(DOUBLE, function("LOG"), required(DOUBLE), optional(INTEGER)); 
	}
	
	default TypedOperator abs() {
		return new TypedOperator(DOUBLE, function("ABS"), required(DOUBLE));
	}

	default TypedOperator ceil() {
		return new TypedOperator(BIGINT, function("CEIL"), required(DOUBLE)); 
	}

	default TypedOperator floor() {
		return new TypedOperator(BIGINT, function("FLOOR"), required(DOUBLE)); 
	}

	default TypedOperator trunc() {
		return new TypedOperator(BIGINT, function("TRUNC"), required(DOUBLE), optional(INTEGER)); 
	}
	
	default TypedOperator round() {
		return new TypedOperator(DOUBLE, function("ROUND"), required(DOUBLE), optional(INTEGER));
	}
	
	default TypedOperator mod() {
		return new TypedOperator(BIGINT, function("MOD"), required(DOUBLE), required(DOUBLE));
	}	
	
	default TypedOperator pow() {
		return new TypedOperator(DOUBLE, function("POW"), required(DOUBLE), required(DOUBLE));
	}

	//bitwise functions
	
	default TypedOperator bitAnd() {
		return new TypedOperator(BIGINT, operator("&"), required(BIGINT), required(BIGINT));
	}
	
	default TypedOperator bitOr() {
		return new TypedOperator(BIGINT, operator("|"), required(BIGINT), required(BIGINT));
	}
	
	default TypedOperator bitXor() {
		return new TypedOperator(BIGINT, operator("^"), required(BIGINT), required(BIGINT));
	}
	
	default TypedOperator bitNot() {
		return new TypedOperator(BIGINT, operator("~"), required(BIGINT));
	}
	
	default TypedOperator bitShiftLeft() {
		return new TypedOperator(BIGINT, operator("<<"), required(BIGINT), required(INTEGER));
	}
	
	default TypedOperator bitShiftRight() {
		return new TypedOperator(BIGINT, operator(">>"), required(BIGINT), required(INTEGER));
	}
	
	//string functions

	default TypedOperator length() {
		return new TypedOperator(INTEGER, function("LENGTH"), required(VARCHAR));
	}
	
	default TypedOperator trim() {
		return new TypedOperator(VARCHAR, function("TRIM"), required(VARCHAR));
	}

	default TypedOperator ltrim() {
		return new TypedOperator(VARCHAR, function("LTRIM"), required(VARCHAR));
	}

	default TypedOperator rtrim() {
		return new TypedOperator(VARCHAR, function("RTRIM"), required(VARCHAR));
	}
	
	default TypedOperator upper() {
		return new TypedOperator(VARCHAR, function("UPPER"), required(VARCHAR));
	}

	default TypedOperator lower() {
		return new TypedOperator(VARCHAR, function("LOWER"), required(VARCHAR));
	}
	
	default TypedOperator initcap() {
		return new TypedOperator(VARCHAR, function("INITCAP"), required(VARCHAR));
	}
	
	default TypedOperator reverse() {
		return new TypedOperator(VARCHAR, function("REVERSE"), required(VARCHAR));
	}
	
	default TypedOperator left() {
		return new TypedOperator(VARCHAR, function("LEFT"), required(VARCHAR), required(INTEGER));
	}
	
	default TypedOperator right() {
		return new TypedOperator(VARCHAR, function("RIGHT"), required(VARCHAR), required(INTEGER));
	}
	
	default TypedOperator replace() {
		return new TypedOperator(VARCHAR, function("REPLACE"), required(VARCHAR), required(VARCHAR), required(VARCHAR));
	}
	
	default TypedOperator substring() { //int start, int length
		return new TypedOperator(VARCHAR, function("SUBSTRING"), required(VARCHAR), required(INTEGER), required(INTEGER));
	}
	
	default TypedOperator concat() {
		return new TypedOperator(VARCHAR, function("CONCAT"), required(VARCHAR), required(VARCHAR), varargs(VARCHAR));
	}
	
	default TypedOperator lpad() {
		return new TypedOperator(VARCHAR, function("LPAD"), required(BIGINT, VARCHAR), required(INTEGER), required(VARCHAR));
	}
	
	default TypedOperator rpad() {
		return new TypedOperator(VARCHAR, function("RPAD"), required(BIGINT, VARCHAR), required(INTEGER), required(VARCHAR));
	}

	default TypedOperator age() { //td interval type
		return new TypedOperator(VARCHAR, function("AGE"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE), optional(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}

	//temporal functions
	
	default TypedOperator year() {
		return new TypedOperator(INTEGER, extract("YEAR"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	default TypedOperator month() {
		return new TypedOperator(INTEGER, extract("MONTH"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}

	default TypedOperator week() {
		return new TypedOperator(INTEGER, extract("WEEK"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}
	
	default TypedOperator day() {
		return new TypedOperator(INTEGER, extract("DAY"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	default TypedOperator dow() {
		return new TypedOperator(INTEGER, extract("DOW"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	default TypedOperator doy() {
		return new TypedOperator(INTEGER, extract("DOY"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}

	default TypedOperator hour() {
		return new TypedOperator(INTEGER, extract("HOUR"), required(TIME, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}

	default TypedOperator minute() {
		return new TypedOperator(INTEGER, extract("MINUTE"), required(TIME, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	default TypedOperator second() {
		return new TypedOperator(INTEGER, extract("SECOND"), required(TIME, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	default TypedOperator epoch() {
		return new TypedOperator(BIGINT, extract("EPOCH"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); //!Teradata
	}
	
	//combined functions
	
	default TypedOperator semester() {//[1-2]
		CombinedOperator op = args-> month().operation(requireNArgs(1, args, ()-> "semester")[0]).toCase()
				.when(lt(7), 1)
				.orElse(2);
		return new TypedOperator(INTEGER, op, required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}
	
	default TypedOperator quarter() {//[1-4]
		CombinedOperator op = args-> month().operation(requireNArgs(1, args, ()-> "quarter")[0]).toCase()
				.when(lt(4), 1)
				.when(lt(7), 2)
				.when(lt(10), 3)
				.orElse(4);
		return new TypedOperator(INTEGER, op, required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}
	
	default TypedOperator yearSemester() {//YYYY-'S'S
		CombinedOperator op = args-> concat().operation(
				varchar().operation(year().operation(args)),
				"-S",
				varchar().operation(semester().operation(args)));
		return new TypedOperator(VARCHAR, op, required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}
	
	default TypedOperator yearQuarter() {//YYYY-'Q'Q
		CombinedOperator op = args-> concat().operation(
				varchar().operation(year().operation(args)),
				"-Q",
				varchar().operation(quarter().operation(args)));
		return new TypedOperator(VARCHAR, op, required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}
	
	default TypedOperator yearWeek() {//YYYY-'W'WW
		CombinedOperator op = args-> {
			var col = requireNArgs(1, args, ()-> "yearWeek")[0];
			return concat().operation(varchar().operation(year().operation(col)), 
					"-W", 
					lpad().operation(varchar().operation(doy().operation(col)), 2, "0"));
		};
		return new TypedOperator(VARCHAR, op, required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	default TypedOperator yearMonth() {//YYYY-MM
		CombinedOperator op = args-> left().operation(varchar().operation(requireNArgs(1, args, ()-> "yearMonth")[0]), 7);
		return new TypedOperator(VARCHAR, op, required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}
	
	
	default TypedOperator monthDay() {//MM-DD
		CombinedOperator op = args-> {
			var col = requireNArgs(1, args, ()-> "monthDay")[0];
			return substring().operation(varchar().operation(col), 6, 5);
		};
		return new TypedOperator(VARCHAR, op, required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	default TypedOperator hourMinute() {//HH:MM
		CombinedOperator op = args-> {
			var col = requireNArgs(1, args, ()-> "hourMinute")[0];
			var tim = JDBCType.typeOf(col).filter(t-> t == TIME)
					.map(t-> col).orElseGet(()-> time().operation(col));
			return left().operation(varchar().operation(tim), 5);
		};
		return new TypedOperator(VARCHAR, op, required(TIME, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}

	default TypedOperator and() {
		return chain(AND);
	}

	default TypedOperator or() {
		return chain(OR);
	}
	
	private TypedOperator chain(LogicalOperator op) {
		CombinedOperator co = args-> {
			requireNArgs(2, args, op::name);
			return ((Criteria)args[0]).append(op, (Criteria)args[1]);
		};
		return new TypedOperator(BOOLEAN, co, required(BOOLEAN), required(BOOLEAN));
	}
	
	//cast functions

	default TypedOperator varchar() {
		return new TypedOperator(VARCHAR, cast("VARCHAR"), required(), optional(INTEGER)); //any
	}
	
	default TypedOperator timestamp() {
		return new TypedOperator(TIMESTAMP, cast("TIMESTAMP"), required(VARCHAR, DATE)); 
	}
	
	default TypedOperator date() {
		return new TypedOperator(DATE, cast("DATE"), required(VARCHAR, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}

	default TypedOperator time() {
		return new TypedOperator(TIME, cast("TIME"), required(VARCHAR, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	default TypedOperator integer() {
		return new TypedOperator(INTEGER, cast("INTEGER"), required(VARCHAR, DOUBLE));
	}
	
	default TypedOperator bigint() {
		return new TypedOperator(BIGINT, cast("BIGINT"), required(VARCHAR, DOUBLE)); // any number
	}
	
	default TypedOperator decimal() {
		return new TypedOperator(DOUBLE, cast("DECIMAL"), required(VARCHAR, BIGINT), optional(INTEGER), optional(INTEGER));
	}

	default TypedOperator bool() {
		return new TypedOperator(BOOLEAN, cast("BOOLEAN"), required(VARCHAR, BIGINT)); //any
	}

	//other functions
	
	default TypedOperator coalesce() {
		return new TypedOperator(firstArgJdbcType(), function("COALESCE"), required(), required(firstArgJdbcType()));
	}
	
	default TypedOperator distinct() {
		return new TypedOperator(firstArgJdbcType(), new DistinctOperator(), required());
	}

	//aggregate functions

	default TypedOperator count() {
		return new TypedOperator(BIGINT, aggregation("COUNT"), required()); 
	}
	
	default TypedOperator min() {
		return new TypedOperator(firstArgJdbcType(), aggregation("MIN"), required()); 
	}

	default TypedOperator max() {
		return new TypedOperator(firstArgJdbcType(), aggregation("MAX"), required()); 
	}

	default TypedOperator sum() {
		return new TypedOperator(DOUBLE, aggregation("SUM"), required(DOUBLE));
	}
	
	default TypedOperator avg() {
		return new TypedOperator(DOUBLE, aggregation("AVG"), required(DOUBLE));
	}
	
	//window functions
	
	default TypedOperator rank() {
		return new TypedOperator(INTEGER, window("RANK")); // takes no args
	}
	
	default TypedOperator rowNumber() {
		return new TypedOperator(INTEGER, window("ROW_NUMBER")); // takes no args
	}
	
	default TypedOperator denseRank() {
		return new TypedOperator(INTEGER, window("DENSE_RANK")); // takes no args
	}

	default TypedOperator percentRank() {
		return new TypedOperator(INTEGER, window("PERCENT_RANK")); // takes no args
	}

	//pipe functions
	
	default TypedOperator over() {
		return new TypedOperator(firstArgJdbcType(), pipe("OVER"), required(), optional(PARTITION)); //optional !?
	}
	
	// constant operators
	
	default TypedOperator cdate() {
		return new TypedOperator(DATE, constant("CURRENT_DATE"));
	}
	
	default TypedOperator ctime() {
		return new TypedOperator(TIME, constant("CURRENT_TIME"));
	}
	
	default TypedOperator ctimestamp() {
		return new TypedOperator(TIMESTAMP, constant("CURRENT_TIMESTAMP"));
	}

	public static ArithmeticOperator operator(String symbol) {
		return ()-> symbol;
	}

	public static FunctionOperator function(String name) {
		return ()-> name;
	}
	
	public static ExtractFunction extract(String field) {
		return ()-> field;
	}

	public static CastFunction cast(String type) {
		return ()-> type;
	}
	
	public static WindowFunction window(String name) {
		return ()-> name;
	}
	
	public static AggregateFunction aggregation(String name) {
		return ()-> name;
	}

	public static PipeFunction pipe(String name) {
		return ()-> name;
	}

	public static ConstantOperator constant(String name) {
		return ()-> name;
	}	
}

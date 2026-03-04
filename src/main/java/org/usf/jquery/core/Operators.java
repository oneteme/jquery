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
public class Operators {
	
	public static final Operators STD_OPERATORS = new Operators();
	
	//Arithmetic operations

	public TypedOperator plus() {
		return new TypedOperator(firstArgJdbcType(), operator("+"), required(), required(firstArgJdbcType()));
	}

	public TypedOperator minus() {
		return new TypedOperator(firstArgJdbcType(), operator("-"), required(), required(firstArgJdbcType())); //date|datetime
	}

	public TypedOperator multiply() {
		return new TypedOperator(firstArgJdbcType(), operator("*"), required(), required(firstArgJdbcType()));
	}
	
	public TypedOperator divide() {
		return new TypedOperator(firstArgJdbcType(), operator("/"), required(), required(firstArgJdbcType()));
	}
	
	//numeric functions
	
	public TypedOperator sqrt() {
		return new TypedOperator(DOUBLE, function("SQRT"), required(DOUBLE)); 
	}
	
	public TypedOperator exp() {
		return new TypedOperator(DOUBLE, function("EXP"), required(DOUBLE)); 
	}
	
	public TypedOperator log() {
		return new TypedOperator(DOUBLE, function("LOG"), required(DOUBLE), optional(INTEGER)); 
	}
	
	public TypedOperator abs() {
		return new TypedOperator(DOUBLE, function("ABS"), required(DOUBLE));
	}

	public TypedOperator ceil() {
		return new TypedOperator(BIGINT, function("CEIL"), required(DOUBLE)); 
	}

	public TypedOperator floor() {
		return new TypedOperator(BIGINT, function("FLOOR"), required(DOUBLE)); 
	}

	public TypedOperator trunc() {
		return new TypedOperator(BIGINT, function("TRUNC"), required(DOUBLE), optional(INTEGER)); 
	}
	
	public TypedOperator round() {
		return new TypedOperator(DOUBLE, function("ROUND"), required(DOUBLE), optional(INTEGER));
	}
	
	public TypedOperator mod() {
		return new TypedOperator(BIGINT, function("MOD"), required(DOUBLE), required(DOUBLE));
	}	
	
	public TypedOperator pow() {
		return new TypedOperator(DOUBLE, function("POW"), required(DOUBLE), required(DOUBLE));
	}

	//bitwise functions
	
	public TypedOperator bitAnd() {
		return new TypedOperator(BIGINT, operator("&"), required(BIGINT), required(BIGINT));
	}
	
	public TypedOperator bitOr() {
		return new TypedOperator(BIGINT, operator("|"), required(BIGINT), required(BIGINT));
	}
	
	public TypedOperator bitXor() {
		return new TypedOperator(BIGINT, operator("^"), required(BIGINT), required(BIGINT));
	}
	
	public TypedOperator bitNot() {
		return new TypedOperator(BIGINT, operator("~"), required(BIGINT));
	}
	
	public TypedOperator bitShiftLeft() {
		return new TypedOperator(BIGINT, operator("<<"), required(BIGINT), required(INTEGER));
	}
	
	public TypedOperator bitShiftRight() {
		return new TypedOperator(BIGINT, operator(">>"), required(BIGINT), required(INTEGER));
	}
	
	//string functions

	public TypedOperator length() {
		return new TypedOperator(INTEGER, function("LENGTH"), required(VARCHAR));
	}
	
	public TypedOperator trim() {
		return new TypedOperator(VARCHAR, function("TRIM"), required(VARCHAR));
	}

	public TypedOperator ltrim() {
		return new TypedOperator(VARCHAR, function("LTRIM"), required(VARCHAR));
	}

	public TypedOperator rtrim() {
		return new TypedOperator(VARCHAR, function("RTRIM"), required(VARCHAR));
	}
	
	public TypedOperator upper() {
		return new TypedOperator(VARCHAR, function("UPPER"), required(VARCHAR));
	}

	public TypedOperator lower() {
		return new TypedOperator(VARCHAR, function("LOWER"), required(VARCHAR));
	}
	
	public TypedOperator initcap() {
		return new TypedOperator(VARCHAR, function("INITCAP"), required(VARCHAR));
	}
	
	public TypedOperator reverse() {
		return new TypedOperator(VARCHAR, function("REVERSE"), required(VARCHAR));
	}
	
	public TypedOperator left() {
		return new TypedOperator(VARCHAR, function("LEFT"), required(VARCHAR), required(INTEGER));
	}
	
	public TypedOperator right() {
		return new TypedOperator(VARCHAR, function("RIGHT"), required(VARCHAR), required(INTEGER));
	}
	
	public TypedOperator replace() {
		return new TypedOperator(VARCHAR, function("REPLACE"), required(VARCHAR), required(VARCHAR), required(VARCHAR));
	}
	
	public TypedOperator substring() { //int start, int length
		return new TypedOperator(VARCHAR, function("SUBSTRING"), required(VARCHAR), required(INTEGER), required(INTEGER));
	}
	
	public TypedOperator concat() {
		return new TypedOperator(VARCHAR, function("CONCAT"), required(VARCHAR), required(VARCHAR), varargs(VARCHAR));
	}
	
	public TypedOperator lpad() {
		return new TypedOperator(VARCHAR, function("LPAD"), required(BIGINT, VARCHAR), required(INTEGER), required(VARCHAR));
	}
	
	public TypedOperator rpad() {
		return new TypedOperator(VARCHAR, function("RPAD"), required(BIGINT, VARCHAR), required(INTEGER), required(VARCHAR));
	}

	public TypedOperator age() { //td interval type
		return new TypedOperator(VARCHAR, function("AGE"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE), optional(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}

	//temporal functions
	
	public TypedOperator year() {
		return new TypedOperator(INTEGER, extract("YEAR"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	public TypedOperator month() {
		return new TypedOperator(INTEGER, extract("MONTH"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}

	public TypedOperator week() {
		return new TypedOperator(INTEGER, extract("WEEK"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}
	
	public TypedOperator day() {
		return new TypedOperator(INTEGER, extract("DAY"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	public TypedOperator dow() {
		return new TypedOperator(INTEGER, extract("DOW"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	public TypedOperator doy() {
		return new TypedOperator(INTEGER, extract("DOY"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}

	public TypedOperator hour() {
		return new TypedOperator(INTEGER, extract("HOUR"), required(TIME, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}

	public TypedOperator minute() {
		return new TypedOperator(INTEGER, extract("MINUTE"), required(TIME, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	public TypedOperator second() {
		return new TypedOperator(INTEGER, extract("SECOND"), required(TIME, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	public TypedOperator epoch() {
		return new TypedOperator(BIGINT, extract("EPOCH"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); //!Teradata
	}
	
	//combined functions
	
	public TypedOperator semester() {//[1-2]
		CombinedOperator op = args-> month().operation(requireNArgs(1, args, ()-> "semester")[0]).toCase()
				.when(lt(7), 1)
				.orElse(2);
		return new TypedOperator(INTEGER, op, required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}
	
	public TypedOperator quarter() {//[1-4]
		CombinedOperator op = args-> month().operation(requireNArgs(1, args, ()-> "quarter")[0]).toCase()
				.when(lt(4), 1)
				.when(lt(7), 2)
				.when(lt(10), 3)
				.orElse(4);
		return new TypedOperator(INTEGER, op, required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}
	
	public TypedOperator yearSemester() {//YYYY-'S'S
		CombinedOperator op = args-> concat().operation(
				varchar().operation(year().operation(args)),
				"-S",
				varchar().operation(semester().operation(args)));
		return new TypedOperator(VARCHAR, op, required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}
	
	public TypedOperator yearQuarter() {//YYYY-'Q'Q
		CombinedOperator op = args-> concat().operation(
				varchar().operation(year().operation(args)),
				"-Q",
				varchar().operation(quarter().operation(args)));
		return new TypedOperator(VARCHAR, op, required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}
	
	public TypedOperator yearWeek() {//YYYY-'W'WW
		CombinedOperator op = args-> {
			var col = requireNArgs(1, args, ()-> "yearWeek")[0];
			return concat().operation(varchar().operation(year().operation(col)), 
					"-W", 
					lpad().operation(varchar().operation(doy().operation(col)), 2, "0"));
		};
		return new TypedOperator(VARCHAR, op, required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	public TypedOperator yearMonth() {//YYYY-MM
		CombinedOperator op = args-> left().operation(varchar().operation(requireNArgs(1, args, ()-> "yearMonth")[0]), 7);
		return new TypedOperator(VARCHAR, op, required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}
	
	
	public TypedOperator monthDay() {//MM-DD
		CombinedOperator op = args-> {
			var col = requireNArgs(1, args, ()-> "monthDay")[0];
			return substring().operation(varchar().operation(col), 6, 5);
		};
		return new TypedOperator(VARCHAR, op, required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	public TypedOperator hourMinute() {//HH:MM
		CombinedOperator op = args-> {
			var col = requireNArgs(1, args, ()-> "hourMinute")[0];
			var tim = JDBCType.typeOf(col).filter(t-> t == TIME)
					.map(t-> col).orElseGet(()-> time().operation(col));
			return left().operation(varchar().operation(tim), 5);
		};
		return new TypedOperator(VARCHAR, op, required(TIME, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}

	public TypedOperator and() {
		return chain(AND);
	}

	public TypedOperator or() {
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

	public TypedOperator varchar() {
		return new TypedOperator(VARCHAR, cast("VARCHAR"), required(), optional(INTEGER)); //any
	}
	
	public TypedOperator timestamp() {
		return new TypedOperator(TIMESTAMP, cast("TIMESTAMP"), required(VARCHAR, DATE)); 
	}
	
	public TypedOperator date() {
		return new TypedOperator(DATE, cast("DATE"), required(VARCHAR, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}

	public TypedOperator time() {
		return new TypedOperator(TIME, cast("TIME"), required(VARCHAR, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	public TypedOperator integer() {
		return new TypedOperator(INTEGER, cast("INTEGER"), required(VARCHAR, DOUBLE));
	}
	
	public TypedOperator bigint() {
		return new TypedOperator(BIGINT, cast("BIGINT"), required(VARCHAR, DOUBLE)); // any number
	}
	
	public TypedOperator decimal() {
		return new TypedOperator(DOUBLE, cast("DECIMAL"), required(VARCHAR, BIGINT), optional(INTEGER), optional(INTEGER));
	}

	public TypedOperator bool() {
		return new TypedOperator(BOOLEAN, cast("BOOLEAN"), required(VARCHAR, BIGINT)); //any
	}

	//other functions
	
	public TypedOperator coalesce() {
		return new TypedOperator(firstArgJdbcType(), function("COALESCE"), required(), required(firstArgJdbcType()));
	}
	
	public TypedOperator distinct() {
		return new TypedOperator(firstArgJdbcType(), new DistinctOperator(), required());
	}

	//aggregate functions

	public TypedOperator count() {
		return new TypedOperator(BIGINT, aggregation("COUNT"), required()); 
	}
	
	public TypedOperator min() {
		return new TypedOperator(firstArgJdbcType(), aggregation("MIN"), required()); 
	}

	public TypedOperator max() {
		return new TypedOperator(firstArgJdbcType(), aggregation("MAX"), required()); 
	}

	public TypedOperator sum() {
		return new TypedOperator(DOUBLE, aggregation("SUM"), required(DOUBLE));
	}
	
	public TypedOperator avg() {
		return new TypedOperator(DOUBLE, aggregation("AVG"), required(DOUBLE));
	}
	
	//window functions
	
	public TypedOperator rank() {
		return new TypedOperator(INTEGER, window("RANK")); // takes no args
	}
	
	public TypedOperator rowNumber() {
		return new TypedOperator(INTEGER, window("ROW_NUMBER")); // takes no args
	}
	
	public TypedOperator denseRank() {
		return new TypedOperator(INTEGER, window("DENSE_RANK")); // takes no args
	}

	public TypedOperator percentRank() {
		return new TypedOperator(INTEGER, window("PERCENT_RANK")); // takes no args
	}

	//pipe functions
	
	public TypedOperator over() {
		return new TypedOperator(firstArgJdbcType(), pipe("OVER"), required(), optional(PARTITION)); //optional !?
	}
	
	// constant operators
	
	public TypedOperator cdate() {
		return new TypedOperator(DATE, constant("CURRENT_DATE"));
	}
	
	public TypedOperator ctime() {
		return new TypedOperator(TIME, constant("CURRENT_TIME"));
	}
	
	public TypedOperator ctimestamp() {
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
	
	public static Operators of(ProductVendor db) {
		return STD_OPERATORS;
	}
}

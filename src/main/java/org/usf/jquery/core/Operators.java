package org.usf.jquery.core;

import static org.usf.jquery.core.JDBCType.BIGINT;
import static org.usf.jquery.core.JDBCType.BOOLEAN;
import static org.usf.jquery.core.JDBCType.DATE;
import static org.usf.jquery.core.JDBCType.DOUBLE;
import static org.usf.jquery.core.JDBCType.INTEGER;
import static org.usf.jquery.core.JDBCType.TIME;
import static org.usf.jquery.core.JDBCType.TIMESTAMP;
import static org.usf.jquery.core.JDBCType.TIMESTAMP_WITH_TIMEZONE;
import static org.usf.jquery.core.JDBCType.VARCHAR;
import static org.usf.jquery.core.Parameter.optional;
import static org.usf.jquery.core.Parameter.required;
import static org.usf.jquery.core.Parameter.varargs;
import static org.usf.jquery.core.TypeResolver.firstArgType;

/**
 * 
 * @author u$f
 *
 */
public interface Operators {
	
	//Arithmetic operations

	default OperatorDefinition plus() {
		return new OperatorDefinition(firstArgType(), operator("+"), required(), required(firstArgType()));
	}

	default OperatorDefinition minus() {
		return new OperatorDefinition(firstArgType(), operator("-"), required(), required(firstArgType())); //date|datetime
	}

	default OperatorDefinition multiply() {
		return new OperatorDefinition(firstArgType(), operator("*"), required(), required(firstArgType()));
	}
	
	default OperatorDefinition divide() {
		return new OperatorDefinition(firstArgType(), operator("/"), required(), required(firstArgType()));
	}
	
	//numeric functions
	
	default OperatorDefinition sqrt() {
		return new OperatorDefinition(DOUBLE, function("SQRT"), required(DOUBLE)); 
	}
	
	default OperatorDefinition exp() {
		return new OperatorDefinition(DOUBLE, function("EXP"), required(DOUBLE)); 
	}
	
	default OperatorDefinition log() {
		return new OperatorDefinition(DOUBLE, function("LOG"), required(DOUBLE), optional(INTEGER)); 
	}
	
	default OperatorDefinition abs() {
		return new OperatorDefinition(DOUBLE, function("ABS"), required(DOUBLE));
	}

	default OperatorDefinition ceil() {
		return new OperatorDefinition(BIGINT, function("CEIL"), required(DOUBLE)); 
	}

	default OperatorDefinition floor() {
		return new OperatorDefinition(BIGINT, function("FLOOR"), required(DOUBLE)); 
	}

	default OperatorDefinition trunc() {
		return new OperatorDefinition(BIGINT, function("TRUNC"), required(DOUBLE), optional(INTEGER)); 
	}
	
	default OperatorDefinition round() {
		return new OperatorDefinition(DOUBLE, function("ROUND"), required(DOUBLE), optional(INTEGER));
	}
	
	default OperatorDefinition mod() {
		return new OperatorDefinition(BIGINT, function("MOD"), required(DOUBLE), required(DOUBLE));
	}	
	
	default OperatorDefinition pow() {
		return new OperatorDefinition(DOUBLE, function("POW"), required(DOUBLE), required(DOUBLE));
	}

	//bitwise functions
	
	default OperatorDefinition bitAnd() {
		return new OperatorDefinition(BIGINT, operator("&"), required(BIGINT), required(BIGINT));
	}
	
	default OperatorDefinition bitOr() {
		return new OperatorDefinition(BIGINT, operator("|"), required(BIGINT), required(BIGINT));
	}
	
	default OperatorDefinition bitXor() {
		return new OperatorDefinition(BIGINT, operator("^"), required(BIGINT), required(BIGINT));
	}
	
	default OperatorDefinition bitNot() {
		return new OperatorDefinition(BIGINT, operator("~"), required(BIGINT));
	}
	
	default OperatorDefinition bitShiftLeft() {
		return new OperatorDefinition(BIGINT, operator("<<"), required(BIGINT), required(INTEGER));
	}
	
	default OperatorDefinition bitShiftRight() {
		return new OperatorDefinition(BIGINT, operator(">>"), required(BIGINT), required(INTEGER));
	}
	
	//string functions

	default OperatorDefinition length() {
		return new OperatorDefinition(INTEGER, function("LENGTH"), required(VARCHAR));
	}
	
	default OperatorDefinition trim() {
		return new OperatorDefinition(VARCHAR, function("TRIM"), required(VARCHAR));
	}

	default OperatorDefinition ltrim() {
		return new OperatorDefinition(VARCHAR, function("LTRIM"), required(VARCHAR));
	}

	default OperatorDefinition rtrim() {
		return new OperatorDefinition(VARCHAR, function("RTRIM"), required(VARCHAR));
	}
	
	default OperatorDefinition upper() {
		return new OperatorDefinition(VARCHAR, function("UPPER"), required(VARCHAR));
	}

	default OperatorDefinition lower() {
		return new OperatorDefinition(VARCHAR, function("LOWER"), required(VARCHAR));
	}
	
	default OperatorDefinition initcap() {
		return new OperatorDefinition(VARCHAR, function("INITCAP"), required(VARCHAR));
	}
	
	default OperatorDefinition reverse() {
		return new OperatorDefinition(VARCHAR, function("REVERSE"), required(VARCHAR));
	}
	
	default OperatorDefinition left() {
		return new OperatorDefinition(VARCHAR, function("LEFT"), required(VARCHAR), required(INTEGER));
	}
	
	default OperatorDefinition right() {
		return new OperatorDefinition(VARCHAR, function("RIGHT"), required(VARCHAR), required(INTEGER));
	}
	
	default OperatorDefinition replace() {
		return new OperatorDefinition(VARCHAR, function("REPLACE"), required(VARCHAR), required(VARCHAR), required(VARCHAR));
	}
	
	default OperatorDefinition substring() { //int start, int length
		return new OperatorDefinition(VARCHAR, function("SUBSTRING"), required(VARCHAR), required(INTEGER), required(INTEGER));
	}
	
	default OperatorDefinition concat() {
		return new OperatorDefinition(VARCHAR, function("CONCAT"), required(VARCHAR), required(VARCHAR), varargs(VARCHAR));
	}
	
	default OperatorDefinition lpad() {
		return new OperatorDefinition(VARCHAR, function("LPAD"), required(BIGINT, VARCHAR), required(INTEGER), required(VARCHAR));
	}
	
	default OperatorDefinition rpad() {
		return new OperatorDefinition(VARCHAR, function("RPAD"), required(BIGINT, VARCHAR), required(INTEGER), required(VARCHAR));
	}

	default OperatorDefinition age() { //td interval type
		return new OperatorDefinition(VARCHAR, function("AGE"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE), optional(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}

	//temporal functions
	
	default OperatorDefinition year() {
		return new OperatorDefinition(INTEGER, extract("YEAR"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	default OperatorDefinition month() {
		return new OperatorDefinition(INTEGER, extract("MONTH"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}

	default OperatorDefinition week() {
		return new OperatorDefinition(INTEGER, extract("WEEK"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}
	
	default OperatorDefinition day() {
		return new OperatorDefinition(INTEGER, extract("DAY"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	default OperatorDefinition dow() {
		return new OperatorDefinition(INTEGER, extract("DOW"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	default OperatorDefinition doy() {
		return new OperatorDefinition(INTEGER, extract("DOY"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}

	default OperatorDefinition hour() {
		return new OperatorDefinition(INTEGER, extract("HOUR"), required(TIME, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}

	default OperatorDefinition minute() {
		return new OperatorDefinition(INTEGER, extract("MINUTE"), required(TIME, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	default OperatorDefinition second() {
		return new OperatorDefinition(INTEGER, extract("SECOND"), required(TIME, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	default OperatorDefinition epoch() {
		return new OperatorDefinition(BIGINT, extract("EPOCH"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); //!Teradata
	}
	
	//cast functions

	default OperatorDefinition varchar() {
		return new OperatorDefinition(VARCHAR, cast("VARCHAR"), required(), optional(INTEGER)); //any
	}
	
	default OperatorDefinition timestamp() {
		return new OperatorDefinition(TIMESTAMP, cast("TIMESTAMP"), required(VARCHAR, DATE)); 
	}
	
	default OperatorDefinition date() {
		return new OperatorDefinition(DATE, cast("DATE"), required(VARCHAR, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}

	default OperatorDefinition time() {
		return new OperatorDefinition(TIME, cast("TIME"), required(VARCHAR, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	default OperatorDefinition integer() {
		return new OperatorDefinition(INTEGER, cast("INTEGER"), required(VARCHAR, DOUBLE));
	}
	
	default OperatorDefinition bigint() {
		return new OperatorDefinition(BIGINT, cast("BIGINT"), required(VARCHAR, DOUBLE)); // any number
	}
	
	default OperatorDefinition decimal() {
		return new OperatorDefinition(DOUBLE, cast("DECIMAL"), required(VARCHAR, BIGINT), optional(INTEGER), optional(INTEGER));
	}

	default OperatorDefinition bool() {
		return new OperatorDefinition(BOOLEAN, cast("BOOLEAN"), required(VARCHAR, BIGINT)); //any
	}

	//other functions
	
	default OperatorDefinition coalesce() {
		return new OperatorDefinition(firstArgType(), function("COALESCE"), required(), required(firstArgType()));
	}
	
	default OperatorDefinition distinct() {
		return new OperatorDefinition(firstArgType(), new DistinctOperator(), required());
	}

	//aggregate functions

	default OperatorDefinition count() {
		return new OperatorDefinition(BIGINT, aggregation("COUNT"), required()); 
	}
	
	default OperatorDefinition min() {
		return new OperatorDefinition(firstArgType(), aggregation("MIN"), required()); 
	}

	default OperatorDefinition max() {
		return new OperatorDefinition(firstArgType(), aggregation("MAX"), required()); 
	}

	default OperatorDefinition sum() {
		return new OperatorDefinition(DOUBLE, aggregation("SUM"), required(DOUBLE));
	}
	
	default OperatorDefinition avg() {
		return new OperatorDefinition(DOUBLE, aggregation("AVG"), required(DOUBLE));
	}
	
	default OperatorDefinition percentile() { //TODO continue|discrete
		return new OperatorDefinition(DOUBLE, aggregation("PERCENTILE_CONT"), required(DOUBLE));
	}
	
	default OperatorDefinition median() {
		return new OperatorDefinition(DOUBLE, aggregation("MEDIAN"), required(DOUBLE));
	}
	
	default OperatorDefinition mode() {
		return new OperatorDefinition(firstArgType(), aggregation("MODE"), required());
	}
		
	//window functions
	
	default OperatorDefinition rank() {
		return new OperatorDefinition(INTEGER, window("RANK")); // takes no args
	}
	
	default OperatorDefinition rowNumber() {
		return new OperatorDefinition(INTEGER, window("ROW_NUMBER")); // takes no args
	}
	
	default OperatorDefinition denseRank() {
		return new OperatorDefinition(INTEGER, window("DENSE_RANK")); // takes no args
	}

	default OperatorDefinition percentRank() {
		return new OperatorDefinition(INTEGER, window("PERCENT_RANK")); // takes no args
	}
	
	// constant operators
	
	default OperatorDefinition cdate() {
		return new OperatorDefinition(DATE, constant("CURRENT_DATE"));
	}
	
	default OperatorDefinition ctime() {
		return new OperatorDefinition(TIME, constant("CURRENT_TIME"));
	}
	
	default OperatorDefinition ctimestamp() {
		return new OperatorDefinition(TIMESTAMP, constant("CURRENT_TIMESTAMP"));
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

	public static ConstantOperator constant(String name) {
		return ()-> name;
	}
}

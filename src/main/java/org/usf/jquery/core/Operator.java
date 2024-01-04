package org.usf.jquery.core;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.Optional.empty;
import static org.usf.jquery.core.JDBCType.BIGINT;
import static org.usf.jquery.core.JDBCType.DATE;
import static org.usf.jquery.core.JDBCType.DOUBLE;
import static org.usf.jquery.core.JDBCType.INTEGER;
import static org.usf.jquery.core.JDBCType.TIMESTAMP;
import static org.usf.jquery.core.JDBCType.TIMESTAMP_WITH_TIMEZONE;
import static org.usf.jquery.core.JDBCType.VARCHAR;
import static org.usf.jquery.core.JDBCType.typeOf;
import static org.usf.jquery.core.JqueryType.CLAUSE;
import static org.usf.jquery.core.JqueryType.COLUMN;
import static org.usf.jquery.core.JqueryType.ORDER;
import static org.usf.jquery.core.OverClause.clauses;
import static org.usf.jquery.core.Parameter.optional;
import static org.usf.jquery.core.Parameter.required;
import static org.usf.jquery.core.Parameter.varargs;
import static org.usf.jquery.core.Validation.requireAtLeastNArgs;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * 
 * @author u$f
 *
 */
public interface Operator extends DBProcessor, NestedSql {
	
	String id();
	
	default OperationColumn args(Object... args) {
		return new OperationColumn(this, args); // no type
	}
	
	//Arithmetic operations

	static TypedOperator plus() {
		return new TypedOperator(DOUBLE, operator("+"), required(DOUBLE), required(DOUBLE));
	}

	static TypedOperator minus() {
		return new TypedOperator(DOUBLE, operator("-"), required(DOUBLE), required(DOUBLE)); //date|datetime
	}

	static TypedOperator multiply() {
		return new TypedOperator(DOUBLE, operator("*"), required(DOUBLE), required(DOUBLE));
	}
	
	static TypedOperator divise() {
		return new TypedOperator(DOUBLE, operator("/"), required(DOUBLE), required(DOUBLE));
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
		return new TypedOperator(VARCHAR, function("REPLACE"), required(VARCHAR), required(VARCHAR), required(VARCHAR)); //!teradata
	}
	
	static TypedOperator oreplace() {
		return new TypedOperator(VARCHAR, function("OREPLACE"), required(VARCHAR), required(VARCHAR), required(VARCHAR)); //teradata 
	}
	
	static TypedOperator substring() { //int start, int length
		return new TypedOperator(VARCHAR, function("SUBSTRING"), required(VARCHAR), required(INTEGER), required(INTEGER));
	}
	
	static TypedOperator concat() {
		return new TypedOperator(VARCHAR, function("CONCAT"), required(VARCHAR), required(VARCHAR), varargs(VARCHAR));
	}

	//temporal functions
	
	static TypedOperator year() {
		return new TypedOperator(INTEGER, extract("YEAR"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	static TypedOperator month() {
		return new TypedOperator(INTEGER, extract("MONTH"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}

	static TypedOperator week() {
		return new TypedOperator(INTEGER, extract("WEEK"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	static TypedOperator day() {
		return new TypedOperator(INTEGER, extract("DAY"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	static TypedOperator dow() {
		return new TypedOperator(INTEGER, extract("DOW"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); //!Teradata
	}
	
	static TypedOperator doy() {
		return new TypedOperator(INTEGER, extract("DOY"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); //!Teradata
	}

	static TypedOperator hour() {
		return new TypedOperator(INTEGER, extract("HOUR"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}

	static TypedOperator minute() {
		return new TypedOperator(INTEGER, extract("MINUTE"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	static TypedOperator second() {
		return new TypedOperator(INTEGER, extract("SECOND"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	static TypedOperator epoch() {
		return new TypedOperator(BIGINT, extract("EPOCH"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); //!Teradata
	}

	//cast functions

	static TypedOperator varchar() {
		return new TypedOperator(VARCHAR, cast("VARCHAR"), required(), required(INTEGER)); //any
	}
	
	static TypedOperator date() {
		return new TypedOperator(DATE, cast("DATE"), required(VARCHAR, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
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
		return new TypedOperator(firstArgType(), function("COALESCE"), required(), required());
	}

	//aggregate functions

	static TypedOperator count() {
		return new TypedOperator(BIGINT, aggregation("COUNT"), required()); 
	}
	
	static TypedOperator min() {
		return new TypedOperator(firstArgType(), aggregation("MIN"), required()); 
	}

	static TypedOperator max() {
		return new TypedOperator(firstArgType(), aggregation("MAX"), required()); 
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
		return new TypedOperator(firstArgType(), pipe("OVER"), required(COLUMN), optional(CLAUSE), optional(CLAUSE)) {
			
			@Override
			public OperationColumn args(Object... args) {
				return super.args(args).aggregation(false); //over aggregation functions
			}
			
			@Override
			Object[] mapArg(Object... args) { //map args after check
				var c = Stream.of(args).skip(1).toArray(OperationColumn[]::new);
				return super.mapArg(args[0], clauses(c));
			}
		};
	}

	//clause functions
	
	static TypedOperator partition() {
		return new TypedOperator(CLAUSE, clause("PARTITION BY"), required(COLUMN), varargs(COLUMN));
	}

	static TypedOperator order() {
		return new TypedOperator(CLAUSE, clause("ORDER BY"), required(ORDER), varargs(ORDER));
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

	static ClauseFunction clause(String name) {
		return ()-> name;
	}

	static StandaloneFunction constant(String name) {
		return ()-> name;
	}

	static Optional<TypedOperator> lookupWindowFunction(String op) {
		return lookupOperator(op).filter(fn-> fn.unwrap().getClass() == WindowFunction.class); //!aggregation
	}
	
	static Optional<TypedOperator> lookupOperator(String op) {
		try {
			var m = Operator.class.getMethod(op);
			if(isStatic(m.getModifiers()) && m.getReturnType() == TypedOperator.class) { // no private static
				return Optional.of((TypedOperator) m.invoke(null));
			}
		} catch (Exception e) {/* do not throw exception */}
		return empty();
	}

	private static Function<Object[], JavaType> firstArgType() {
		return arr-> typeOf(requireAtLeastNArgs(1, arr, ()-> "firstArgType function")[0]);
	}
}

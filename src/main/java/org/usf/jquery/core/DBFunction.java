package org.usf.jquery.core;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;
import static org.usf.jquery.core.AggregationFunction.aggregationFunction;
import static org.usf.jquery.core.CastFunction.castFunction;
import static org.usf.jquery.core.ExtractFunction.extractFunction;
import static org.usf.jquery.core.JDBCType.AUTO_TYPE;
import static org.usf.jquery.core.JDBCType.BIGINT;
import static org.usf.jquery.core.JDBCType.DATE;
import static org.usf.jquery.core.JDBCType.DECIMAL;
import static org.usf.jquery.core.JDBCType.INTEGER;
import static org.usf.jquery.core.JDBCType.TIMESTAMP;
import static org.usf.jquery.core.JDBCType.VARCHAR;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.SqlStringBuilder.parenthese;
import static org.usf.jquery.core.TypedFunction.autoTypeReturn;
import static org.usf.jquery.core.Utils.isPresent;
import static org.usf.jquery.core.Validation.illegalArgumentIf;
import static org.usf.jquery.core.Validation.requireNArgs;
import static org.usf.jquery.core.WindowFunction.windowFunction;

import java.util.Optional;
import java.util.function.IntFunction;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface DBFunction extends DBOperation {

	String name();

	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		return sql(builder, args, i-> AUTO_TYPE);
	}

	default String sql(QueryParameterBuilder builder, Object[] args, IntFunction<SQLType> indexedType) { //arg type inject
		return new SqlStringBuilder(name())
				.append("(")
				.appendIf(isPresent(args), ()-> range(0, args.length)
						.mapToObj(i-> builder.appendLitteral(args[i], indexedType.apply(i)))
						.collect(joining(SCOMA))) //accept any
				.append(")")
				.toString();
	}
	
	default OperationColumn args(Object... args) {
		return new OperationColumn(this, args);
	}
	
	//numeric funct.

	static TypedFunction trunc() {
		return new TypedFunction(INTEGER, function("TRUNC"), DECIMAL); 
	}

	static TypedFunction ceil() {
		return new TypedFunction(INTEGER, function("CEIL"), DECIMAL); 
	}

	static TypedFunction floor() {
		return new TypedFunction(INTEGER, function("FLOOR"), DECIMAL); 
	}
	
	static TypedFunction sqrt() {
		return new TypedFunction(DECIMAL, function("SQRT"), DECIMAL); 
	}
	
	static TypedFunction exp() {
		return new TypedFunction(DECIMAL, function("EXP"), DECIMAL); 
	}
	
	static TypedFunction log() {
		return new TypedFunction(DECIMAL, function("LOG"), DECIMAL); 
	}
	
	static TypedFunction abs() {
		return autoTypeReturn(function("ABS"), DECIMAL); //INTEGER | DECIMAL
	}
	
	static TypedFunction mod() {
		return autoTypeReturn(function("MOD"), DECIMAL, DECIMAL);  //INTEGER | DECIMAL
	}
	
	//string funct.

	static TypedFunction length() {
		return new TypedFunction(INTEGER, function("LENGTH"), VARCHAR);
	}
	
	static TypedFunction trim() {
		return new TypedFunction(VARCHAR, function("TRIM"), VARCHAR);
	}

	static TypedFunction ltrim() {
		return new TypedFunction(VARCHAR, function("LTRIM"), VARCHAR);
	}

	static TypedFunction rtrim() {
		return new TypedFunction(VARCHAR, function("RTRIM"), VARCHAR);
	}
	
	static TypedFunction upper() {
		return new TypedFunction(VARCHAR, function("UPPER"), VARCHAR);
	}

	static TypedFunction lower() {
		return new TypedFunction(VARCHAR, function("LOWER"), VARCHAR);
	}
	
	static TypedFunction initcap() {
		return new TypedFunction(VARCHAR, function("INITCAP"), VARCHAR);
	}
	
	static TypedFunction reverse() {
		return new TypedFunction(VARCHAR, function("REVERSE"), VARCHAR);
	}
	
	static TypedFunction left() {
		return new TypedFunction(VARCHAR, function("LEFT"), VARCHAR, INTEGER);
	}
	
	static TypedFunction right() {
		return new TypedFunction(VARCHAR, function("RIGHT"), VARCHAR, INTEGER);
	}
	
	static TypedFunction replace() { //int start, int length
		return new TypedFunction(VARCHAR, function("REPLACE"), VARCHAR, VARCHAR, VARCHAR); //!teradata
	}
	
	static TypedFunction oreplace() { //int start, int length
		return new TypedFunction(VARCHAR, function("OREPLACE"), VARCHAR, VARCHAR, VARCHAR); //teradata 
	}
	
	static TypedFunction substring() { //int start, int length
		return new TypedFunction(VARCHAR, function("SUBSTRING"), VARCHAR, INTEGER, INTEGER);
	}

	//temporal funct.
	
	static TypedFunction year() {
		return new TypedFunction(INTEGER, extractFunction("YEAR"), AUTO_TYPE); //DATE & TIMESTAMP
	}
	
	static TypedFunction month() {
		return new TypedFunction(INTEGER, extractFunction("MONTH"), AUTO_TYPE);
	}

	static TypedFunction week() {
		return new TypedFunction(INTEGER, extractFunction("WEEK"), AUTO_TYPE);
	}
	
	static TypedFunction day() {
		return new TypedFunction(INTEGER, extractFunction("DAY"), AUTO_TYPE);
	}
	
	static TypedFunction dow() {
		return new TypedFunction(INTEGER, extractFunction("DOW"), AUTO_TYPE); //!Teradata
	}
	
	static TypedFunction doy() {
		return new TypedFunction(INTEGER, extractFunction("DOY"), AUTO_TYPE); //!Teradata
	}

	static TypedFunction hour() {
		return new TypedFunction(INTEGER, extractFunction("HOUR"), AUTO_TYPE);
	}

	static TypedFunction minute() {
		return new TypedFunction(INTEGER, extractFunction("MINUTE"), AUTO_TYPE);
	}
	
	static TypedFunction second() {
		return new TypedFunction(INTEGER, extractFunction("SECOND"), AUTO_TYPE);
	}
	
	static TypedFunction epoch() {
		return new TypedFunction(INTEGER, extractFunction("EPOCH"), AUTO_TYPE); //!Teradata
	}

	//cast funct.

	static TypedFunction varchar() {
		return new TypedFunction(VARCHAR, castFunction("VARCHAR"), AUTO_TYPE, INTEGER); //any
	}
	
	static TypedFunction date() {
		return new TypedFunction(DATE, castFunction("DATE"), TIMESTAMP); // + string !?
	}
	
	static TypedFunction integer() {
		return new TypedFunction(INTEGER, castFunction("INTEGER"), VARCHAR); // + string !?
	}
	
	static TypedFunction bigint() {
		return new TypedFunction(BIGINT, castFunction("BIGINT"), VARCHAR); // + string !?
	}

	//other funct
	
	static TypedFunction coalesce() {
		return autoTypeReturn(function("COALESCE"), AUTO_TYPE, AUTO_TYPE); //takes 1 or 2 param
	}
	
	//aggregate funct.

	
	static TypedFunction count() {
		return new TypedFunction(BIGINT, aggregationFunction("COUNT"), AUTO_TYPE); 
	}
	
	static TypedFunction min() {
		return autoTypeReturn(aggregationFunction("MIN"), AUTO_TYPE); 
	}

	static TypedFunction max() {
		return autoTypeReturn(aggregationFunction("MAX"), AUTO_TYPE); 
	}

	static TypedFunction sum() {
		return autoTypeReturn(aggregationFunction("SUM"), DECIMAL); //INTEGER | DECIMAL
	}
	
	static TypedFunction avg() {
		return autoTypeReturn(aggregationFunction("AVG"), DECIMAL); //INTEGER | DECIMAL
	}

	//window funct.
	
	static TypedFunction rank() {
		return new TypedFunction(INTEGER, windowFunction("RANK"));
	}
	
	static TypedFunction rowNumber() {
		return new TypedFunction(INTEGER, windowFunction("ROW_NUMBER"));
	}
	
	static TypedFunction denseRank() {
		return new TypedFunction(INTEGER, windowFunction("DENSE_RANK"));
	}
	
	static TypedFunction over() {
		var fn = new DBFunction() {
			
			@Override
			public String name() {
				return "OVER";
			}
			
			@Override
			public String sql(QueryParameterBuilder builder, Object[] args, IntFunction<SQLType> indexedType) {
				requireNArgs(2, args, ()-> "over function"); //NamedColumn | OperationColumn
				illegalArgumentIf(!(args[0] instanceof DBColumn), "over function require DBColumn @1st parameter");
				illegalArgumentIf(!(args[1] instanceof OverClause), "over function require OverClause @2nd parameter");
				return builder.appendParameter(args[0]) + SPACE + name() + parenthese(((OverClause)args[1]).sql(builder));
			}
		};
		return new TypedFunction(AUTO_TYPE, fn, AUTO_TYPE, AUTO_TYPE) {
			@Override
			public OperationColumn args(Object... args) {
				return new OverColumn(this, args); 
			}
		};
	}

	static DBFunction function(final String name) {
		return ()-> name;
	}

	static Optional<TypedFunction> lookupFunction(String fn) {
		try {
			var m = DBFunction.class.getMethod(fn);
			if(isStatic(m.getModifiers()) && m.getReturnType() == TypedFunction.class) { // no private static
				return Optional.of((TypedFunction) m.invoke(null));
			}
		} catch (Exception e) {/*do not throw exception*/}
		return empty();
	}
}

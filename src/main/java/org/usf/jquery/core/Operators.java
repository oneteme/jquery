package org.usf.jquery.core;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.usf.jquery.core.JDBCType.BIGINT;
import static org.usf.jquery.core.JDBCType.BOOLEAN;
import static org.usf.jquery.core.JDBCType.DATE;
import static org.usf.jquery.core.JDBCType.DOUBLE;
import static org.usf.jquery.core.JDBCType.INTEGER;
import static org.usf.jquery.core.JDBCType.TIME;
import static org.usf.jquery.core.JDBCType.TIMESTAMP;
import static org.usf.jquery.core.JDBCType.TIMESTAMP_WITH_TIMEZONE;
import static org.usf.jquery.core.JDBCType.VARCHAR;
import static org.usf.jquery.core.JQueryType.GROUP;
import static org.usf.jquery.core.JQueryType.PARTITION;
import static org.usf.jquery.core.OperatorKind.AGGREGATE;
import static org.usf.jquery.core.OperatorKind.CAST;
import static org.usf.jquery.core.OperatorKind.DEFAUTL;
import static org.usf.jquery.core.OperatorKind.EXTRACT;
import static org.usf.jquery.core.OperatorKind.OPR;
import static org.usf.jquery.core.OperatorKind.SCOPE;
import static org.usf.jquery.core.OperatorKind.WINDOW;
import static org.usf.jquery.core.Parameter.optional;
import static org.usf.jquery.core.Parameter.required;
import static org.usf.jquery.core.Parameter.varargs;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.TypeResolver.firstArgType;
import static org.usf.jquery.core.Utils.isEmpty;

/**
 * 
 * @author u$f
 *
 */
public interface Operators {
	
	//Arithmetic operations

	default OperatorDefinition plus() {
		return operator(firstArgType(), "plus", "+", required(), required(firstArgType()));
	}

	default OperatorDefinition minus() {
		return operator(firstArgType(), "minus", "-", required(), required(firstArgType())); //date|datetime
	}

	default OperatorDefinition multiply() {
		return operator(firstArgType(), "multiply", "*", required(), required(firstArgType()));
	}
	
	default OperatorDefinition divide() {
		return operator(firstArgType(), "divide", "/", required(), required(firstArgType()));
	}
	
	//numeric functions
	
	default OperatorDefinition sqrt() {
		return function(DOUBLE, "SQRT", required(DOUBLE)); 
	}
	
	default OperatorDefinition exp() {
		return function(DOUBLE, "EXP", required(DOUBLE)); 
	}
	
	default OperatorDefinition log() {
		return function(DOUBLE, "LOG", required(DOUBLE), optional(INTEGER)); 
	}
	
	default OperatorDefinition abs() {
		return function(DOUBLE, "ABS", required(DOUBLE));
	}

	default OperatorDefinition ceil() {
		return function(BIGINT, "CEIL", required(DOUBLE)); 
	}

	default OperatorDefinition floor() {
		return function(BIGINT, "FLOOR", required(DOUBLE)); 
	}

	default OperatorDefinition trunc() {
		return function(BIGINT, "TRUNC", required(DOUBLE), optional(INTEGER)); 
	}
	
	default OperatorDefinition round() {
		return function(DOUBLE, "ROUND", required(DOUBLE), optional(INTEGER));
	}
	
	default OperatorDefinition mod() {
		return function(BIGINT, "MOD", required(DOUBLE), required(DOUBLE));
	}	
	
	default OperatorDefinition pow() {
		return function(DOUBLE, "POW", required(DOUBLE), required(DOUBLE));
	}

	default OperatorDefinition factorial() {
		return singleArgOperator(BIGINT, "factorial", "!", false, required(BIGINT));
	}
	
	//bitwise functions

	default OperatorDefinition bitNot() {
		return singleArgOperator(BIGINT, "bitNot", "~", true, required(BIGINT));
	}
	
	default OperatorDefinition bitAnd() {
		return operator(BIGINT, "bitAnd", "&", required(BIGINT), required(BIGINT));
	}
	
	default OperatorDefinition bitOr() {
		return operator(BIGINT, "bitOr", "|", required(BIGINT), required(BIGINT));
	}
	
	default OperatorDefinition bitXor() {
		return operator(BIGINT, "bitXor", "#", required(BIGINT), required(BIGINT));
	}
	
	default OperatorDefinition bitShiftLeft() {
		return operator(BIGINT, "bitShiftLeft", "<<", required(BIGINT), required(INTEGER));
	}
	
	default OperatorDefinition bitShiftRight() {
		return operator(BIGINT, "bitShiftRight", ">>", required(BIGINT), required(INTEGER));
	}
	
	//string functions

	default OperatorDefinition length() {
		return function(INTEGER, "LENGTH", required(VARCHAR));
	}
	
	default OperatorDefinition trim() {
		return function(VARCHAR, "TRIM", required(VARCHAR));
	}

	default OperatorDefinition ltrim() {
		return function(VARCHAR, "LTRIM", required(VARCHAR));
	}

	default OperatorDefinition rtrim() {
		return function(VARCHAR, "RTRIM", required(VARCHAR));
	}
	
	default OperatorDefinition upper() {
		return function(VARCHAR, "UPPER", required(VARCHAR));
	}

	default OperatorDefinition lower() {
		return function(VARCHAR, "LOWER", required(VARCHAR));
	}
	
	default OperatorDefinition initcap() {
		return function(VARCHAR, "INITCAP", required(VARCHAR));
	}
	
	default OperatorDefinition reverse() {
		return function(VARCHAR, "REVERSE", required(VARCHAR));
	}
	
	default OperatorDefinition left() {
		return function(VARCHAR, "LEFT", required(VARCHAR), required(INTEGER));
	}
	
	default OperatorDefinition right() {
		return function(VARCHAR, "RIGHT", required(VARCHAR), required(INTEGER));
	}
	
	default OperatorDefinition replace() {
		return function(VARCHAR, "REPLACE", required(VARCHAR), required(VARCHAR), required(VARCHAR));
	}
	
	default OperatorDefinition substring() { //int start, int length
		return function(VARCHAR, "SUBSTRING", required(VARCHAR), required(INTEGER), required(INTEGER));
	}
	
	default OperatorDefinition concat() {
		return function(VARCHAR, "CONCAT", required(VARCHAR), required(VARCHAR), varargs(VARCHAR));
	}
	
	default OperatorDefinition lpad() {
		return function(VARCHAR, "LPAD", required(BIGINT, VARCHAR), required(INTEGER), required(VARCHAR));
	}
	
	default OperatorDefinition rpad() {
		return function(VARCHAR, "RPAD", required(BIGINT, VARCHAR), required(INTEGER), required(VARCHAR));
	}

	default OperatorDefinition age() { //td interval type
		return function(VARCHAR, "AGE", required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE), optional(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}

	//temporal functions
	
	default OperatorDefinition year() {
		return extract(INTEGER, "YEAR", required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	default OperatorDefinition month() {
		return extract(INTEGER, "MONTH", required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}

	default OperatorDefinition week() {
		return extract(INTEGER, "WEEK", required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}
	
	default OperatorDefinition day() {
		return extract(INTEGER, "DAY", required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	default OperatorDefinition dow() {
		return extract(INTEGER, "DOW", required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	default OperatorDefinition doy() {
		return extract(INTEGER, "DOY", required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}

	default OperatorDefinition hour() {
		return extract(INTEGER, "HOUR", required(TIME, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}

	default OperatorDefinition minute() {
		return extract(INTEGER, "MINUTE", required(TIME, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	default OperatorDefinition second() {
		return extract(INTEGER, "SECOND", required(TIME, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	default OperatorDefinition epoch() {
		return extract(INTEGER, "EPOCH", required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); //!Teradata
	}
	
	//cast functions

	default OperatorDefinition varchar() {
		return cast(VARCHAR, "VARCHAR", required(), optional(INTEGER)); //any
	}
	
	default OperatorDefinition timestamp() {
		return cast(TIMESTAMP, "TIMESTAMP", required(VARCHAR, DATE)); 
	}
	
	default OperatorDefinition date() {
		return cast(DATE, "DATE", required(VARCHAR, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}

	default OperatorDefinition time() {
		return cast(TIME, "TIME", required(VARCHAR, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	default OperatorDefinition integer() {
		return cast(INTEGER, "INTEGER", required(VARCHAR, DOUBLE));
	}
	
	default OperatorDefinition bigint() {
		return cast(BIGINT, "BIGINT", required(VARCHAR, DOUBLE)); // any number
	}
	
	default OperatorDefinition decimal() {
		return cast(DOUBLE, "DECIMAL", required(VARCHAR, BIGINT), optional(INTEGER), optional(INTEGER));
	}

	default OperatorDefinition bool() {
		return cast(BOOLEAN, "BOOLEAN", required(VARCHAR, BIGINT)); //any
	}

	//other functions
	
	default OperatorDefinition coalesce() {
		return function(firstArgType(), "COALESCE", required(), required(firstArgType()));
	}
	
	default OperatorDefinition distinct() {
		return function(firstArgType(), "DISTINCT", required(), varargs());
	}

	//aggregate functions

	default OperatorDefinition count() {
		return aggregate(BIGINT, "COUNT", required()); 
	}
	
	default OperatorDefinition min() {
		return aggregate(firstArgType(), "MIN", required()); 
	}

	default OperatorDefinition max() {
		return aggregate(firstArgType(), "MAX", required()); 
	}

	default OperatorDefinition sum() {
		return aggregate(DOUBLE, "SUM", required(DOUBLE));
	}
	
	default OperatorDefinition avg() {
		return aggregate(DOUBLE, "AVG", required(DOUBLE));
	}
	
	default OperatorDefinition percentileCont() {
		return aggregate(DOUBLE, "PERCENTILE_CONT", required(DOUBLE));
	}
	
	default OperatorDefinition percentileDisc() {
		return aggregate(DOUBLE, "PERCENTILE_DISC", required(DOUBLE));
	}
	
	default OperatorDefinition median() {
		return aggregate(DOUBLE, "MEDIAN", required(DOUBLE));
	}
	
	default OperatorDefinition mode() {
		return aggregate(firstArgType(), "MODE", required());
	}
		
	//window functions
	
	default OperatorDefinition rank() {
		return window(INTEGER, "RANK"); // takes no args
	}
	
	default OperatorDefinition rowNumber() {
		return window(INTEGER, "ROW_NUMBER"); // takes no args
	}
	
	default OperatorDefinition denseRank() {
		return window(INTEGER, "DENSE_RANK"); // takes no args
	}

	default OperatorDefinition percentRank() {
		return window(INTEGER, "PERCENT_RANK"); // takes no args
	}
	
	// constant operators

	default OperatorDefinition cdate() {
		return constant(DATE, "CURRENT_DATE");
	}
	
	default OperatorDefinition ctime() {
		return constant(TIME, "CURRENT_TIME");
	}
	
	default OperatorDefinition ctimestamp() {
		return constant(TIMESTAMP, "CURRENT_TIMESTAMP");
	}

	default OperatorDefinition pi() {
		return constant(DOUBLE, "PI", true);
	}
	
	default OperatorDefinition random() {
		return constant(DOUBLE, "RANDOM", true);
	}
	
	//scope operators
	
	default OperatorDefinition over() {
		return scope(firstArgType(), "OVER", required(), optional(PARTITION)); 
	}
	
	default OperatorDefinition within() {
		return scope(firstArgType(), "WITHIN GROUP", required(), optional(GROUP));
	}

	public static OperatorDefinition operator(TypeResolver type, String name, String symbol, Parameter... parameters) {
		if(isNull(parameters) || parameters.length != 2 || !parameters[0].isRequired() || !parameters[1].isRequired()) {
			throw new IllegalArgumentException(format("'%s(%s)' must have exactly two required parameters", name, symbol));
		}
		return new OperatorDefinition(name, type, OPR,
				(builder,args)-> builder.append("(").appendParameter(args[0]).append(symbol).appendParameter(args[1]).append(")"),
				parameters);
	}
	
	public static OperatorDefinition singleArgOperator(TypeResolver type, String name, String symbol, boolean prefix, Parameter... parameters) {
		if(isEmpty(parameters) || !parameters[0].isRequired()) {
			throw new IllegalArgumentException(format("'%s(%s)' must have at least one required parameter", name, symbol));
		}
		return new OperatorDefinition(name, type, OPR, prefix
				? (builder,args)-> builder.append(symbol).appendParameter(args[0])
				: (builder,args)-> builder.appendParameter(args[0]).append(symbol),
				parameters);
	}

	public static OperatorDefinition function(TypeResolver type, String name, Parameter... parameters) {
		return function(type, name, DEFAUTL, parameters);
	}

	public static OperatorDefinition window(TypeResolver type, String name, Parameter... parameters) {
		return function(type, name, WINDOW, parameters);
	}

	public static OperatorDefinition aggregate(TypeResolver type, String name, Parameter... parameters) {
		return function(type, name, AGGREGATE, parameters);
	}
	
	public static OperatorDefinition function(TypeResolver type, String name, OperatorKind kind, Parameter... parameters) {
		return new OperatorDefinition(name, type, kind,
				(builder,args)-> builder.append(name).append("(").appendParameters(SCOMA, args, 0).append(")"),
				parameters);
	}
	
	public static OperatorDefinition extract(TypeResolver type, String field, Parameter... parameters) {
		if(isEmpty(parameters) || !parameters[0].isRequired()) {
			throw new IllegalArgumentException(format("'extract(%s)' must have at least one required parameter", field));
		}
		return new OperatorDefinition(field, type, EXTRACT,
				(builder,args)-> builder.append("EXTRACT").append("(").append(field).append(" FROM ").appendParameter(args[0]).append(")"),
				parameters);
	}

	public static OperatorDefinition cast(TypeResolver type, String target, Parameter... parameters) {
		if(isEmpty(parameters) || !parameters[0].isRequired()) {
			throw new IllegalArgumentException(format("'cast(%s)' must have at least one required parameter", target));
		}
		return new OperatorDefinition(target, type, CAST,
				(builder,args)-> builder.append("CAST").appendParenthesis(()-> {
					builder.appendParameter(args[0]).appendAs().append(target);
					if(args.length > 1) { //varchar | decimal
						builder.append("(").appendParameters(SCOMA, args, 1).append(")");
					}
				}), 
				parameters);
	}

	public static OperatorDefinition constant(JDBCType type, String name) {
		return constant(type, name, false);
	}
	
	public static OperatorDefinition constant(JDBCType type, String name, boolean parenthes) {
		return new OperatorDefinition(name, type, DEFAUTL, (builder,args)-> {
			builder.append(name);
			if(parenthes) {
				builder.append("()");
			}
		});
	}

	public static OperatorDefinition scope(TypeResolver resolver, String name, Parameter... parameters) {
		if(isEmpty(parameters) || !parameters[0].isRequired()) {
			throw new IllegalArgumentException(format("'%s' must have at least one required parameter", name));
		}
		return new OperatorDefinition(name, resolver, SCOPE, 
				(builder,args)-> builder.appendParameter(args[0]).appendSpace()
				.append(name).append("(").appendParameters(SCOMA, args, 1).append(")"), 
				parameters);
	}
}

package org.usf.jquery.core;

import static java.lang.reflect.Modifier.isStatic;
import static java.sql.Types.BIGINT;
import static java.sql.Types.DATE;
import static java.sql.Types.DOUBLE;
import static java.sql.Types.INTEGER;
import static java.sql.Types.VARCHAR;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.Utils.isPresent;

import java.util.Optional;
import java.util.stream.Stream;

import org.usf.jquery.core.QueryParameterBuilder.Appender;

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
		return new SqlStringBuilder(name())
				.append("(")
				.appendIf(isPresent(args), ()-> appendParameters(builder, args)) //accept any
				.append(")")
				.toString();
	}
	
	default String appendParameters(QueryParameterBuilder builder, Object[] args) {
		return Stream.of(args)
				.map(builder::appendParameter)
				.collect(joining(SCOMA));
	}
	
	default OperationColumn args(Object... args) {
		return new OperationColumn(this, args);
	}
	
	//aggregate funct.
	
	static TypedFunction count() {
		return new TypedFunction("COUNT", true, QueryParameterBuilder::appendParameter, DOUBLE); 
	}

	static TypedFunction sum() {
		return new TypedFunction("SUM", true, QueryParameterBuilder::appendParameter, DOUBLE); 
	}
	
	static TypedFunction avg() {
		return new TypedFunction("AVG", true, QueryParameterBuilder::appendParameter, DOUBLE);  
	}

	static TypedFunction min() {
		return new TypedFunction("MIN", true, QueryParameterBuilder::appendNumber); //depends on parameter type
	}

	static TypedFunction max() {
		return new TypedFunction("MAX", true, QueryParameterBuilder::appendNumber); //depends on parameter type
	}
	
	//numeric funct.
	
	static TypedFunction abs() {
		return new TypedFunction("ABS", false, QueryParameterBuilder::appendNumber, DOUBLE); 
	}
	
	static TypedFunction sqrt() {
		return new TypedFunction("SQRT", false, QueryParameterBuilder::appendNumber, DOUBLE); 
	}

	static TypedFunction trunc() {
		return new TypedFunction("TRUNC", false, QueryParameterBuilder::appendNumber); //depends on parameter type
	}

	static TypedFunction ceil() {
		return new TypedFunction("CEIL", false, QueryParameterBuilder::appendNumber, DOUBLE); 
	}

	static TypedFunction floor() {
		return new TypedFunction("FLOOR", false, QueryParameterBuilder::appendNumber, DOUBLE); 
	}
	
	static TypedFunction exp() {
		return new TypedFunction("EXP", false, QueryParameterBuilder::appendNumber, DOUBLE); 
	}
	
	static TypedFunction log() {
		return new TypedFunction("LOG", false, QueryParameterBuilder::appendNumber, DOUBLE); 
	}
	
	static TypedFunction mod() {
		return new TypedFunction("MOD", false, 
				asList(QueryParameterBuilder::appendNumber, QueryParameterBuilder::appendNumber), BIGINT); 
	}
	
	//string funct.

	static TypedFunction length() {
		return new TypedFunction("LENGTH", false, QueryParameterBuilder::appendString, INTEGER);
	}
	
	static TypedFunction trim() {
		return new TypedFunction("TRIM", false, QueryParameterBuilder::appendString, VARCHAR);
	}
	
	static TypedFunction upper() {
		return new TypedFunction("UPPER", false, QueryParameterBuilder::appendString, VARCHAR);
	}

	static TypedFunction lower() {
		return new TypedFunction("LOWER", false, QueryParameterBuilder::appendString, VARCHAR);
	}
	
	static TypedFunction initcap() {
		return new TypedFunction("INITCAP", false, QueryParameterBuilder::appendString, VARCHAR);
	}

	//temporal funct.
	
	static TypedFunction year() {
		return extract("YEAR");
	}
	
	static TypedFunction month() {
		return extract("MONTH");
	}

	static TypedFunction week() {
		return extract("WEEK");
	}
	
	static TypedFunction day() {
		return extract("DAY");
	}
	
	static TypedFunction dow() {
		return extract("DOW");
	}
	
	static TypedFunction doy() {
		return extract("DOY");
	}

	static TypedFunction hour() {
		return extract("HOUR");
	}

	static TypedFunction minute() {
		return extract("MINUTE");
	}
	
	static TypedFunction second() {
		return extract("SECOND");
	}
	
	static TypedFunction epoch() {
		return extract("EPOCH");
	}
	
	static TypedFunction date() {
		return cast("DATE", QueryParameterBuilder::appendTimestamp, DATE);
	}

	private static TypedFunction extract(String field) {
		return new TypedFunction("EXTRACT", false, QueryParameterBuilder::appendTimestamp, INTEGER)
				.argsPrefix(field + " FROM ");
	}
	
	static TypedFunction cast(String type, Appender appender, int returnedType) {
		return new TypedFunction("CAST", false, appender, returnedType)
				.argsSuffix(" AS " + type);
	}

	
	static DBFunction function(final String name) {
		return ()-> name;
	}

	static Optional<TypedFunction> lookup(String fucntion) {
		try {
			var m = DBFunction.class.getMethod(fucntion.toLowerCase());
			if(isStatic(m.getModifiers()) && m.getReturnType().isAssignableFrom(TypedFunction.class)) { // no private static
				return Optional.of((TypedFunction) m.invoke(null));
			}
		} catch (Exception e) {
			//do nothing here
		}
		return empty();
	}
	
}

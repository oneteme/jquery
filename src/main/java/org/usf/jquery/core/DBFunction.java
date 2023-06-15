package org.usf.jquery.core;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.Utils.isPresent;

import java.util.Optional;
import java.util.stream.Stream;

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
		return new TypedFunction("COUNT", true, QueryParameterBuilder::appendParameter); 
	}

	static TypedFunction sum() {
		return new TypedFunction("SUM", true, QueryParameterBuilder::appendParameter); 
	}
	
	static TypedFunction avg() {
		return new TypedFunction("AVG", true, QueryParameterBuilder::appendParameter);  
	}

	static TypedFunction min() {
		return new TypedFunction("MIN", true, QueryParameterBuilder::appendNumber);
	}

	static TypedFunction max() {
		return new TypedFunction("MAX", true, QueryParameterBuilder::appendNumber);
	}
	
	//numeric funct.
	
	static TypedFunction abs() {
		return new TypedFunction("ABS", false, QueryParameterBuilder::appendNumber); 
	}
	
	static TypedFunction sqrt() {
		return new TypedFunction("SQRT", false, QueryParameterBuilder::appendNumber); 
	}

	static TypedFunction trunc() {
		return new TypedFunction("TRUNC", false, QueryParameterBuilder::appendNumber); 
	}

	static TypedFunction ceil() {
		return new TypedFunction("CEIL", false, QueryParameterBuilder::appendNumber); 
	}

	static TypedFunction floor() {
		return new TypedFunction("FLOOR", false, QueryParameterBuilder::appendNumber); 
	}
	
	static TypedFunction exp() {
		return new TypedFunction("EXP", false, QueryParameterBuilder::appendNumber); 
	}
	
	static TypedFunction log() {
		return new TypedFunction("LOG", false, QueryParameterBuilder::appendNumber); 
	}
	
	static TypedFunction mod() {
		return new TypedFunction("MOD", false, asList(QueryParameterBuilder::appendNumber, QueryParameterBuilder::appendNumber)); 
	}
	
	//string funct.

	static TypedFunction length() {
		return new TypedFunction("LENGTH", false, QueryParameterBuilder::appendString); //return number !!
	}
	
	static TypedFunction trim() {
		return new TypedFunction("TRIM", false, QueryParameterBuilder::appendString);
	}
	
	static TypedFunction upper() {
		return new TypedFunction("UPPER", false, QueryParameterBuilder::appendString);
	}

	static TypedFunction lower() {
		return new TypedFunction("LOWER", false, QueryParameterBuilder::appendString);
	}
	
	static TypedFunction initcap() {
		return new TypedFunction("INITCAP", false, QueryParameterBuilder::appendString);
	}
	
	static DBFunction function(final String name) {
		return ()-> name;
	}

	static Optional<TypedFunction> lookup(String fucntion) {
		try {
			var m = DBFunction.class.getMethod(fucntion.toLowerCase());
			if(isStatic(m.getModifiers()) && m.getReturnType().equals(TypedFunction.class)) { // no private static
				return Optional.of((TypedFunction) m.invoke(null));
			}
		} catch (Exception e) {
			//do nothing here
		}
		return empty();
	}
	
}

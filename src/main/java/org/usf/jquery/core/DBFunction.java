package org.usf.jquery.core;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.DBColumn.column;
import static org.usf.jquery.core.SqlStringBuilder.COMA;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Utils.isPresent;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

@FunctionalInterface
public interface DBFunction extends DBOperation {

	String name();
	
	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		return new SqlStringBuilder(name())
				.append("(")
				.appendIf(isPresent(args), ()-> appendParameters(builder, args))
				.append(")")
				.toString();
	}
	
	default String appendParameters(QueryParameterBuilder builder, Object[] args) {
		return Stream.of(args)
				.map(builder::appendParameter)
				.collect(joining(COMA));
	}
	
	default OperationColumn args(Object... args) {
		return new OperationColumn(this, args);
	}
	
	//aggregate
	
	static OperationColumn count() {
		return count(column("*"));
	}

	static OperationColumn count(Object arg) {
		return aggregate("COUNT", QueryParameterBuilder::appendParameter, arg);
	}

	static OperationColumn min(Object arg) {
		return aggregate("MIN", QueryParameterBuilder::appendParameter, arg);
	}

	static OperationColumn max(Object arg) {
		return aggregate("MAX", QueryParameterBuilder::appendParameter, arg);
	}

	static OperationColumn sum(Object arg) {
		return aggregate("SUM", QueryParameterBuilder::appendNumber, arg);
	}
	
	static OperationColumn avg(Object arg) {
		return aggregate("AVG", QueryParameterBuilder::appendNumber, arg);
	}
	
	//numeric
	
	static OperationColumn abs(Object arg) {
		return function("ABS", QueryParameterBuilder::appendNumber, arg);
	}
	
	static OperationColumn sqrt(Object arg) {
		return function("SQRT", QueryParameterBuilder::appendNumber, arg);
	}

	static OperationColumn trunc(Object arg) {
		return function("TRUNC", QueryParameterBuilder::appendNumber, arg);
	}

	static OperationColumn ceil(Object arg) {
		return function("CEIL", QueryParameterBuilder::appendNumber, arg);
	}

	static OperationColumn floor(Object arg) {
		return function("FLOOR", QueryParameterBuilder::appendNumber, arg);
	}
	
	//string

	static OperationColumn trim(Object arg) {
		return function("TRIM", QueryParameterBuilder::appendString, arg);
	}

	static OperationColumn length(Object arg) {
		return function("LENGTH", QueryParameterBuilder::appendString, arg);
	}

	static OperationColumn upper(Object arg) {
		return function("UPPER", QueryParameterBuilder::appendString, arg);
	}

	static OperationColumn lower(Object arg) {
		return function("LOWER", QueryParameterBuilder::appendString, arg);
	}
	
	static OperationColumn aggregate(String name, BiFunction<QueryParameterBuilder, Object, String> appender, Object arg) {
		return function(true, name, singletonList(appender), arg);
	}
	
	static OperationColumn function(String name, BiFunction<QueryParameterBuilder, Object, String> appender, Object arg) {
		return function(false, name, singletonList(appender), arg);
	}
	
	static OperationColumn function(boolean aggregate, String name, List<BiFunction<QueryParameterBuilder, Object, String>> appenders, Object... args) {
		if(isEmpty(appenders) && isEmpty(args)) {
			return function(name).args(args); //no arguments
		}
		if(isEmpty(appenders) || isEmpty(args) || appenders.size() != args.length) {
			throw new IllegalArgumentException("appenders.size != args.size");
		}
		return new TypedArgsAppender(name, aggregate, appenders).args(args);
	}
	
	static DBFunction function(final String name) {
		return ()-> name;
	}
	
}

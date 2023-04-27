package org.usf.jquery.core;

import static java.lang.reflect.Array.getLength;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;
import static org.usf.jquery.core.SqlStringBuilder.COMA;
import static org.usf.jquery.core.SqlStringBuilder.EMPTY;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.SqlStringBuilder.varchar;
import static org.usf.jquery.core.Validation.illegalArgumentIf;

import java.lang.reflect.Array;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryParameterBuilder {
	
	private static final String ARG = "?";
	
	private final Collection<Object> args;

	public String appendParameter(Object o) {
		return appendParameter(o, Object.class, v-> v instanceof Number ? formatNumber(v) : formatString(v));
	}
	
	public String appendNumber(Object o) {
		return appendParameter(o, Number.class, QueryParameterBuilder::formatNumber);
	}
	
	public String appendString(Object o) {
		return appendParameter(o, String.class, QueryParameterBuilder::formatString);
	}

	public String appendDate(Object o) {
		return appendParameter(o, Date.class, QueryParameterBuilder::formatString);
	}

	public String appendTimestamp(Object o) {
		return appendParameter(o, Timestamp.class, QueryParameterBuilder::formatString);
	}

	public String appendParameter(Object o, Class<?> type, Function<Object, String> fn) {
		if(o == null) {
			return dynamic() ? appendArg(null) : "null";
		}
		if(o instanceof DBColumn) {
			return ((DBColumn)o).sql(this);
		}
		if(type.isInstance(o)) {
			return dynamic() ? appendArg(o) : fn.apply(o);
		}
		throw new IllegalArgumentException("require " + type.getSimpleName().toLowerCase() + " parameter");
	}

	public String appendArray(Object o) {
		illegalArgumentIf(o == null || !o.getClass().isArray(), ()-> "require array parameter");
		if(dynamic()) {
			streamArray(o).forEach(args::add);
			return nParameter(getLength(o));
		}
		Function<Object, String> fn = o.getClass().getComponentType().isAssignableFrom(Number.class)
				? QueryParameterBuilder::formatNumber
				: QueryParameterBuilder::formatString;
		return streamArray(o).map(fn).collect(joining(SCOMA));
	}
	
	private String appendArg(Object o) {
		args.add(o);
		return ARG;
	}

	String formatValue(Object o) {
		return o instanceof Number 
				? formatNumber(o)
				: formatString(o);
	}
	
	boolean dynamic() {
		return nonNull(args);
	}
	
	public Object[] args() {
		return dynamic() ? args.toArray() : new Object[0];
	}

	static Stream<Object> streamArray(Object o) {
		return range(0, getLength(o))
			.mapToObj(i-> Array.get(o, i));
	}

	static String nParameter(int n){
		if(n < 1){
			return EMPTY;
		}
        return n == 1 ? ARG : ARG + (COMA + ARG).repeat(n-1);
    }
	
	static String formatString(Object o) {
		return varchar(o.toString());
	}
	static String formatNumber(Object o) {
		return o.toString(); 
	}
	
	public static QueryParameterBuilder addWithValue() {
		return new QueryParameterBuilder(null); //no args
	}
	
	public static QueryParameterBuilder parametrized() {
		return new QueryParameterBuilder(new LinkedList<>());
	}
}

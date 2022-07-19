package org.usf.jquery.core;

import static java.lang.reflect.Array.getLength;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;
import static org.usf.jquery.core.SqlStringBuilder.varchar;
import static org.usf.jquery.core.Validation.illegalArgumentIf;
import static org.usf.jquery.core.Validation.illegalArgumentIfNot;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryParameterBuilder {
	
	private static final String ARG = "?";
	
	private final Collection<Object> args;
	
	public String appendParameter(Object o) {
		if(o == null) {
			return appendNull();
		}
		if(o instanceof DBColumn) {
			return formatColumn((DBColumn)o);
		}
		illegalArgumentIf(o.getClass().isArray(), ()-> "array value");
		return dynamic() ? appendArg(o) : formatValue(o);
	}
	
	public String appendString(Object o) {
		if(o == null ) {
			return appendNull();
		}
		if(o instanceof DBColumn) {
			return formatColumn((DBColumn)o);
		}
		illegalArgumentIfNot(o instanceof String, ()->"require string parameter");
		return dynamic() ? appendArg(o) : formatString(o); 
	}
	
	public String appendNumber(Object o) {
		if(o == null ) {
			return appendNull();
		}
		if(o instanceof DBColumn) {
			return formatColumn((DBColumn)o);
		}
		illegalArgumentIfNot(o instanceof Number, ()->"require number parameter");
		return dynamic() ? appendArg(o) : formatNumber((Number)o);
	}
	
	public String appendArray(Object o) {
		illegalArgumentIf(o == null || !o.getClass().isArray() || getLength(o) == 0, ()-> "not array");
		if(dynamic()) {
			streamArray(o).forEach(args::add);
			return nParameter(getLength(o));
		}
		return streamArray(o).map(QueryParameterBuilder::formatValue).collect(joining(","));
	}
	
	private String appendNull() {
		return dynamic() ? appendArg(null) : "null";
	}
	
	private String appendArg(Object o) {
		args.add(o);
		return ARG;
	}

	String formatColumn(DBColumn o) {
		return o.sql(this);
	}

	static String formatValue(Object o) {
		return o instanceof Number 
				? formatNumber((Number)o)
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
        return n == 1 ? ARG : ARG + ",?".repeat(n-1);
    }
	
	static String formatNumber(Number o) {
		return o.toString();
	}
	
	static String formatString(Object o) {
		return varchar(o.toString());
	}
	
	public static QueryParameterBuilder addWithValue() {
		return new QueryParameterBuilder(null);
	}
	
	public static QueryParameterBuilder parametrized() {
		return new QueryParameterBuilder(new LinkedList<>());
	}
}

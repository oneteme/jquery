package org.usf.jquery.core;

import static java.lang.reflect.Array.getLength;
import static java.sql.Types.BIGINT;
import static java.sql.Types.BIT;
import static java.sql.Types.BOOLEAN;
import static java.sql.Types.CHAR;
import static java.sql.Types.DATE;
import static java.sql.Types.DECIMAL;
import static java.sql.Types.DOUBLE;
import static java.sql.Types.FLOAT;
import static java.sql.Types.INTEGER;
import static java.sql.Types.LONGNVARCHAR;
import static java.sql.Types.NUMERIC;
import static java.sql.Types.NVARCHAR;
import static java.sql.Types.REAL;
import static java.sql.Types.SMALLINT;
import static java.sql.Types.TIME;
import static java.sql.Types.TIMESTAMP;
import static java.sql.Types.TINYINT;
import static java.sql.Types.VARCHAR;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.usf.jquery.core.SqlStringBuilder.COMA;
import static org.usf.jquery.core.SqlStringBuilder.EMPTY;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.SqlStringBuilder.member;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.core.Utils.AUTO_TYPE;
import static org.usf.jquery.core.Utils.isPresent;
import static org.usf.jquery.core.Validation.illegalArgumentIf;

import java.lang.reflect.Array;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryParameterBuilder {
	
	private static final String ARG = "?";
	
	private final Collection<Object> args;
	private List<String> tables = new LinkedList<>();
	private boolean forceValue = false;
	
	public QueryParameterBuilder tables(String... tablenames) {
		if(isPresent(tablenames)){
			tables = Stream.of(tablenames).distinct().collect(toList());
		}
		return this;
	}

	public String columnFullReference(String tablename, String columnRef) {
		return tables.size() < 2 
				? columnRef //don't need alias
				: member(tableAlias(tablename), columnRef);
	}
	
	public String tableAlias(String tablename) {
		var idx = tables.indexOf(tablename) + 1;
		return idx > 0 ? "t"+idx : tablename;
	}

	public String appendParameter(Object o) {
		return appendParameter(o, Object.class);
	}
	
	public String appendNumber(Object o) {
		return appendParameter(o, Number.class);
	}
	
	public String appendString(Object o) {
		return appendParameter(o, String.class);
	}

	public String appendDate(Object o) {
		return appendParameter(o, Date.class);
	}

	public String appendTimestamp(Object o) {
		return appendParameter(o, Timestamp.class);
	}

	public String append(Object o, int type) {
		return appendParameter(o, javaType(type));
	}

	public String appendLitteral(Object o, int type) {
		var cur = forceValue;
		try {
			forceValue(true);
			return appendParameter(o, javaType(type)); 
		}
		finally {
			forceValue(cur);
		}
	}

	private String appendParameter(Object o, Class<?> type) {
		if(o == null) {
			return dynamic() ? appendArg(null) : "null";
		}
		if(o instanceof DBColumn) { //check type !?
			return ((DBColumn)o).sql(this);
		}
		if(type.isInstance(o)) {
			if(dynamic()) {
				return appendArg(o);
			}
			return o instanceof Number ? formatNumber(o) : formatString(o);
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
	
	public void forceValue(boolean forceValue) {
		this.forceValue = forceValue;
	}
	
	boolean dynamic() {
		return nonNull(args) && !forceValue;
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
		return quote(o.toString());
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
	
	static Class<?> javaType(int type){
		switch(type) {
		case AUTO_TYPE		: return Object.class;
		case BOOLEAN		: return Boolean.class;
		case BIT		  	:
		case TINYINT  		:
		case SMALLINT 		:
		case INTEGER  		:
		case BIGINT   		:
		case REAL 	  		:
		case FLOAT	  		:
		case DOUBLE  		:
		case NUMERIC 		: 
		case DECIMAL  		: return Number.class;
		case CHAR  	  		: 
		case VARCHAR  		:
		case NVARCHAR  		:
		case LONGNVARCHAR	: return String.class;
		case DATE     		: return Date.class;
		case TIME     		: return Time.class;
		case TIMESTAMP		: return Timestamp.class; 
		default       		: throw new UnsupportedOperationException("unsupported dbType=" + type + " parse");
		}
	}
}

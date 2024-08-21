package org.usf.jquery.core;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.JDBCType.typeOf;
import static org.usf.jquery.core.SqlStringBuilder.COMA;
import static org.usf.jquery.core.SqlStringBuilder.EMPTY;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 
 * @author u$f
 *
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryParameterBuilder {
	
	private static final String P_ARG = "?";
	
	@Getter
	private final String schema;
	private final String vPrefix;
	private final List<Object> args; //dynamic flag
	private final List<JDBCType> argTypes;
	private final List<DBView> views; //indexed view
	private final Map<DBView, DBView> overView;
		
	public String view(DBView view) {
		view = overView.getOrDefault(view, view);
		var idx = views.indexOf(view);
		if(idx < 0) {
			idx = views.size();
			views.add(view);
		}
		return vPrefix + (idx+1);
	}
	
	public List<DBView> views(){
		return views;
	}
	
	public String appendArrayParameter(Object[] arr) {
		return appendArrayParameter(arr, 0);
	}
	
	public String appendArrayParameter(Object[] arr, int from) {
		return dynamic() 
				? appendArray(arr, from, this::appendParameter) 
				: appendLiteralArray(arr, from);
	}

	public String appendLiteralArray(Object[] arr) {
		return appendLiteralArray(arr, 0);
	}
	
	public String appendLiteralArray(Object[] arr, int from) {
		return appendArray(arr, from, this::appendLiteral);
	}

	String appendArray(Object[] arr, int from, Function<Object, String> fn) {
		if(isEmpty(arr)) {
			if(from == 0) {
				return EMPTY;
			}
		}
		else if(from >= 0 && from < arr.length) {
			return Stream.of(arr)
					.skip(from)
					.map(fn)
					.collect(joining(SCOMA));
		}
		throw new IndexOutOfBoundsException(from);
	}

	public String appendParameter(Object o) {
		if(dynamic()) {
			if(o instanceof DBObject jo) {
				return jo.sql(this, null);
			}
			var t = typeOf(o); //o=null=>empty
			if(t.isPresent()) {
				return appendArg(t.get(), o);
			}
		}
		return appendLiteral(o);
	}

	public String appendLiteral(Object o) {  //TD : stringify value using db default pattern
		return o instanceof DBObject jo 
				? jo.sql(this, null)
				: formatValue(o);
	}
	
	private String appendArg(JDBCType type, Object o) {
		argTypes.add(type);
		args.add(o);
		return P_ARG;
	}
	
	public Object[] args() {
		return dynamic() ? args.toArray() : null;
	}
	
	public int[] argTypes() {
		return dynamic() ? argTypes.stream().mapToInt(JDBCType::getValue).toArray() : null;
	}
	
	private boolean dynamic() {
		return nonNull(args);
	}

	static String nParameter(int n){
		if(n < 1){
			return EMPTY;
		}
        return n == 1 ? P_ARG : P_ARG + (COMA + P_ARG).repeat(n-1);
    }

	public static String formatValue(Object o) {
		if(o instanceof Number){
			return o.toString();
		}
		return nonNull(o) ? quote(o.toString()) : "null";
	}
	
	public QueryParameterBuilder withValue() {
		return new QueryParameterBuilder(schema, vPrefix, null, null, views, overView);
	}
	
	public QueryParameterBuilder subQuery() {
		return new QueryParameterBuilder(schema, vPrefix + "_s", args, argTypes, new ArrayList<>(), emptyMap());
	}

	public static QueryParameterBuilder addWithValue() {
		return addWithValue(null, emptyMap()); //no args
	}

	public static QueryParameterBuilder addWithValue(String schema, Map<DBView, ? extends DBView> overView) {
		return new QueryParameterBuilder(schema, "v", null, null, new ArrayList<>(), unmodifiableMap(overView)); //no args
	}

	public static QueryParameterBuilder parametrized(String schema, Map<DBView, ? extends DBView> overView) {
		return new QueryParameterBuilder(schema, "v", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), unmodifiableMap(overView));
	}
}

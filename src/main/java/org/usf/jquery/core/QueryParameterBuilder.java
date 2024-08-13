package org.usf.jquery.core;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.JDBCType.typeOf;
import static org.usf.jquery.core.SqlStringBuilder.COMA;
import static org.usf.jquery.core.SqlStringBuilder.EMPTY;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author u$f
 *
 */
@Setter(AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryParameterBuilder {
	
	private static final String P_ARG = "?";
	
	@Getter
	private final String schema;
	private final String vPrefix;
	private final List<Object> args;
	private final List<JDBCType> argTypes;
	private final List<DBView> views; //indexed
	private final Map<DBView, DBView> overView = new HashMap<>();
		
	public List<DBView> views(){
		return views;
	}
	
	public String view(DBView view) {
		var idx = views.indexOf(overView.getOrDefault(view, view));
		if(idx < 0) {
			idx = views.size();
			views.add(view);
		}
		return isNull(vPrefix) ? null : vPrefix + (idx+1);
	}
	
	public void overView(DBView oldView, DBView newView) {
		overView.put(oldView, newView);
	}

	public String appendArrayParameter(Object[] arr) {
		return appendArrayParameter(arr, 0);
	}
	
	public String appendArrayParameter(Object[] arr, int from) {
		if(dynamic()) {
			if(isEmpty(arr) || from>=arr.length) { //throw !?
				return EMPTY;
			}
			for(var i=from; i<arr.length; i++){
				appendParameter(arr[i]);
			}
			return nParameter(arr.length-from);
		}
		return appendLiteralArray(arr, from);
	}

	public String appendLiteralArray(Object[] arr) {
		return appendLiteralArray(arr, 0);
	}
	
	public String appendLiteralArray(Object[] arr, int from) {
		return isEmpty(arr) || from>=arr.length ? EMPTY : Stream.of(arr) //throw !?
				.map(this::appendLiteral)
				.collect(joining(SCOMA));
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
		return new QueryParameterBuilder(schema, vPrefix, null, null, views);
	}
	
	public QueryParameterBuilder subQuery() {
		return new QueryParameterBuilder(schema, isNull(vPrefix) ? null : vPrefix + "_s", args, argTypes, new ArrayList<>());
	}

	public static QueryParameterBuilder addWithValue() {
		return new QueryParameterBuilder(null, null, null, null, null); //no args
	}

	public static QueryParameterBuilder parametrized(String schema) {
		return new QueryParameterBuilder(schema, "v", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
	}
}

package org.usf.jquery.core;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.SqlStringBuilder.COMA;
import static org.usf.jquery.core.SqlStringBuilder.EMPTY;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryParameterBuilder {
	
	private static final String ARG = "?";
	
	@Getter
	private final String schema;
	private final String vPrefix;
	private final List<Object> args;
	private final List<JDBCType> argTypes;
	private final List<DBView> views; //indexed
	
	public List<DBView> views(){
		return views;
	}
	
	public String view(@NonNull DBView view) {
		if(isNull(vPrefix)) {
			return null;
		}
		for(var i=0; i<views.size(); i++) {
			if(views.get(i).id().equals(view.id())) {
				return vPrefix + (i+1);
			}
		}
		views.add(view);
		return vPrefix + views.size();
	}
	
	public String appendArrayParameter(JDBCType type, @NonNull Object[] arr) {
		if(dynamic()) {
			if(isEmpty(arr)) {
				return EMPTY;
			}
			for(var o : arr){
				argTypes.add(type);
				args.add(o);
			}
			return nParameter(arr.length);
		}
		return appendLitteralArray(arr);
	}
	
	public String appendLitteralArray(@NonNull Object[] arr) {
		return isEmpty(arr) ? EMPTY : Stream.of(arr)
				.map(this::appendLitteral)
				.collect(joining(SCOMA));
	}

	public String appendParameter(JDBCType type, Object o) {
		if(dynamic()) {
			argTypes.add(type);
			return o instanceof DBObject
					? ((DBObject)o).sql(this, null)
					: appendArg(o);
		}
		return appendLitteral(o);
	}

	public String appendLitteral(Object o) {  //TD : stringify value using db default pattern
		return o instanceof DBObject 
				? ((DBObject)o).sql(this, null)
				: formatValue(o);
	}
	
	private String appendArg(Object o) {
		args.add(o);
		return ARG;
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
        return n == 1 ? ARG : ARG + (COMA + ARG).repeat(n-1);
    }

	public static String formatValue(Object o) {
		if(nonNull(o)){
			return o instanceof Number 
					? o.toString()
					: quote(o.toString());
		}
		return "null";
	}
	
	public QueryParameterBuilder withValue() {
		return new QueryParameterBuilder(schema, vPrefix, null, null, views);
	}
	
	public QueryParameterBuilder subQuery() {
		return new QueryParameterBuilder(schema, isNull(vPrefix) ? null : vPrefix + "_s", args, argTypes, new LinkedList<>());
	}

	public static QueryParameterBuilder addWithValue() {
		return new QueryParameterBuilder(null, null, null, null, null); //no args
	}

	public static QueryParameterBuilder parametrized(List<DBView> views) {
		return parametrized(null, views);
	}
	
	public static QueryParameterBuilder parametrized(String schema, List<DBView> views) {
		return new QueryParameterBuilder(schema, "v", new LinkedList<>(), new LinkedList<>(), views);
	}
}

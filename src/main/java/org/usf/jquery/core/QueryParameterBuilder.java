package org.usf.jquery.core;

import static java.lang.reflect.Array.getLength;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;
import static org.usf.jquery.core.SqlStringBuilder.COMA;
import static org.usf.jquery.core.SqlStringBuilder.EMPTY;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.core.Validation.illegalArgumentIf;

import java.lang.reflect.Array;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntConsumer;
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
	
	private final String viewAlias;
	private final List<Object> args;
	private final List<TaggableView> views; //indexed
	
	public String view(TaggableView view) {
		return view(view, i-> {});
	}
	
	public String overwriteView(TaggableView view) {
		return view(view, i-> views.set(i, view));
	}

	private String view(TaggableView view, IntConsumer consumer) {
		var i=0;
		for(; i<views.size(); i++) {
			if(views.get(i).tagname().equals(view.tagname())) {
				consumer.accept(i);
				return viewAlias + (i+1); //always add alias
			}
		}
		views.add(view);
		return viewAlias + views.size(); //always add alias
	}
	
	public List<TaggableView> views(){
		return views;
	}

	public String appendParameter(Object o) {
		return appendParameter(o, Object.class, false);
	}
	
	public String appendNumber(Object o) {
		return appendParameter(o, Number.class, false);
	}
	
	public String appendString(Object o) {
		return appendParameter(o, String.class, false);
	}

	public String appendDate(Object o) {
		return appendParameter(o, Date.class, false);
	}

	public String appendTimestamp(Object o) {
		return appendParameter(o, Timestamp.class, false);
	}

	public String appendLitteral(Object o, JavaType type) {
		return appendParameter(o, type.type(), true); 
	}
	
	//TODO
	public String appendLitteral(Object o) {
		return appendParameter(o, Object.class, true); 
	}

	private String appendParameter(Object o, Class<?> type, boolean addWithValue) {
		if(isNull(o)) {
			return dynamic() && !addWithValue ? appendArg(null) : "null";
		}
		if(o instanceof DBObject) { //check type !?
			return ((DBObject)o).sql(this, null);
		}
		if(type.isInstance(o)) {
			if(dynamic() && !addWithValue) {
				return appendArg(o);
			}
			return formatValue(o);
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
		return quote(o.toString());
	}
	static String formatNumber(Object o) {
		return o.toString(); 
	}
	
	public static QueryParameterBuilder addWithValue() {
		return new QueryParameterBuilder("s", null, new ArrayList<>()); //no args
	}
	
	public static QueryParameterBuilder addWithValue(QueryParameterBuilder builder) {
		return new QueryParameterBuilder(builder.viewAlias, null, builder.views);
	}
	
	public static QueryParameterBuilder parametrized() {
		return new QueryParameterBuilder("v", new LinkedList<>(), new ArrayList<>());
	}
}

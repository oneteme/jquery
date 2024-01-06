package org.usf.jquery.core;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.SqlStringBuilder.COMA;
import static org.usf.jquery.core.SqlStringBuilder.EMPTY;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.SqlStringBuilder.quote;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.stream.Stream;

import lombok.AccessLevel;
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
	
	private final String viewAlias;
	private final List<Object> args;
	private final List<TaggableView> views; //indexed
	
	public List<TaggableView> views(){
		return views;
	}
	
	public String view(TaggableView view) {
		return view(view, i-> {});
	}
	
	public String overwriteView(TaggableView view) {
		return view(view, i-> views.set(i, view));
	}

	private String view(TaggableView view, IntConsumer consumer) {
		for(var i=0; i<views.size(); i++) {
			if(views.get(i).tagname().equals(view.tagname())) {
				consumer.accept(i);
				return viewAlias + (i+1); //always add alias
			}
		}
		views.add(view);
		return viewAlias + views.size(); //always add alias
	}

	public String appendParameter(Object o) {
		if(dynamic()) {
			return o instanceof DBObject
					? ((DBObject)o).sql(this, null)
					: appendArg(o);
		}
		return appendLitteral(o);
	}

	public String appendLitteral(Object o) {
		return o instanceof DBObject 
				? ((DBObject)o).sql(this, null) 
				: formatValue(o);
	}
	
	public String appendArrayParameter(@NonNull Object[] arr) {
		if(dynamic()) {
			Stream.of(arr).forEach(args::add);
			return nParameter(arr.length);
		}
		return appendArrayParameter(arr);
	}
	
	public String appendLitteralArray(@NonNull Object[] arr) {
		Function<Object, String> fn = arr.getClass().getComponentType().isAssignableFrom(Number.class)
				? QueryParameterBuilder::formatNumber
				: QueryParameterBuilder::formatString;
		return Stream.of(arr).map(fn).collect(joining(SCOMA));
	}
	
	private String appendArg(Object o) {
		args.add(o);
		return ARG;
	}
	
	public Object[] args() {
		return dynamic() ? args.toArray() : new Object[0];
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

	static String formatValue(Object o) {
		return o instanceof Number 
				? formatNumber(o)
				: formatString(o);
	}
	
	static String formatString(Object o) {
		return isNull(o) ? "null" : quote(o.toString());
	}
	static String formatNumber(Object o) {
		return isNull(o) ? "null" : o.toString(); 
	}
	
	public QueryParameterBuilder withValue() {
		return new QueryParameterBuilder(viewAlias, null, views);
	}
	
	public static QueryParameterBuilder addWithValue() {
		return new QueryParameterBuilder("s", null, new ArrayList<>()); //no args
	}
	
	public static QueryParameterBuilder parametrized() {
		return new QueryParameterBuilder("v", new LinkedList<>(), new ArrayList<>());
	}
	
}

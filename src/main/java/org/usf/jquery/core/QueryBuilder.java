package org.usf.jquery.core;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.usf.jquery.core.Environment.NO_ENV;
import static org.usf.jquery.core.JDBCType.typeOf;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.core.TypedArg.arg;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@Getter(value = AccessLevel.PACKAGE)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryBuilder {
	
	private final Environment environment;
	private final String prefix;
	private final StringBuilder query;
	private final Map<QueryView, String> ctes;
	private final Map<DBView, String> views;
	private final List<TypedArg> args;
	private final Map<DBView, QueryView> overViews;
	private final Object currentModel;

	Optional<QueryView> subView(DBView view) {
		return ofNullable(overViews.get(view));
	}
	
	public boolean isCte(DBView view) {
		return view instanceof QueryView q && ctes.containsKey(q);
	}
	
	public QueryBuilder appendViewAlias(DBView view) {
		return appendViewAlias(view, "");
	}
	
	public QueryBuilder appendViewAlias(DBView view, String after) {
		if(!ctes.isEmpty() || !views.isEmpty()) {
			var v = ctes.containsKey(view) ? ctes.get(view) : views.get(view);
			if(nonNull(v)) {
				append(v).append(after); //view.
			}
			else if(view.getClass() != ViewRef.class) {
				log.warn("alias not found for view=" + view);
			}
		} //else no alias
		return this;
	}
	
	public QueryBuilder append(String sql) {
		query.append(sql);
		return this;
	}
	
	public QueryBuilder append(DBObject o) {
		o.build(this);
		return this;
	}
	
	public QueryBuilder appendSpace() {
		return append(SPACE);
	}
	
	public QueryBuilder appendAs() {
		return append(" AS ");
	}
	
	public QueryBuilder appendParenthesis(Runnable exec) {
		append("(");
		exec.run();
		return append(")");
	}

	public QueryBuilder appendParameters(String delimiter, Object[] arr) {
		return appendParameters(delimiter, arr, 0, false);
	}
	
	public QueryBuilder appendParameters(String delimiter, Object[] arr, boolean parameterized) {
		return appendParameters(delimiter, arr, 0, parameterized);
	}
	
	public QueryBuilder appendParameters(String delimiter, Object[] arr, int from) {
		return appendParameters(delimiter, arr, from, false);
	}
	
	public QueryBuilder appendParameters(String delimiter, Object[] arr, int from, boolean parameterized) {
		return runForeach(delimiter, arr, from, o-> appendParameter(o, parameterized));
	}
	
	public QueryBuilder appendParameter(Object o) {
		return appendParameter(o, false);
	}

	public QueryBuilder appendParameter(Object o, boolean parameterized) { //comparators
		if(o instanceof DBObject jo) {
			return append(jo);
		}
		if(parameterized && isParameterized()) {
			var t = typeOf(o);
			if(t.isPresent()) {
				args.add(arg(o, t.get().getValue()));
				return append("?");
			}
		}
		if(o instanceof Number){
			return append(o.toString());
		} 
		return append(nonNull(o) ? quote(o.toString()) : "null");   //TD : stringify value using db default pattern
	}

	public QueryBuilder appendEach(String delimiter, DBObject[] arr) {
		return runForeach(delimiter, arr, 0, o-> o.build(this));
	}
	
	public <T> QueryBuilder appendEach(String delimiter, T[] arr, Consumer<T> cons) {
		return runForeach(delimiter, arr, 0, cons);
	}

	public QueryBuilder withValue() { //inherit schema, prefix, args but not views
		return new QueryBuilder(environment, prefix, query, ctes, views, null, overViews, currentModel); //no args
	}

	public QueryBuilder withModel(Object model) { //overwrite model only
		return new QueryBuilder(environment, prefix, query, ctes, views, args, overViews, model);
	}
	
	public QueryBuilder subQuery(DBView[] views, Map<DBView, QueryView> overview) { //inherit schema, prefix, args but not views
		var s = prefix + "_s";
		var vMap = viewAlias(s, views);
		if(!isEmpty(overview)) {
			overview.forEach((k,v)-> vMap.put(k, ctes.get(v))); //override or add
		}
		return new QueryBuilder(environment, s, query, ctes, vMap, args, overview, currentModel);
	}
	
	public Query build() {
		return build(NO_ENV);
	}
	
	public Query build(Environment env) {
		return new Query(env, query.toString(), isParameterized() ? args.toArray(TypedArg[]::new) : null);
	}

	private boolean isParameterized() {
		return nonNull(args);
	}
	
	private <T> QueryBuilder runForeach(String delimiter, T[] arr, int idx, Consumer<T> fn) {
		requireNonNull(arr, "arr connot be null");
		if(idx < arr.length) {
			if(!isEmpty(arr)) {
				var i=idx;
				fn.accept(arr[i]);
				for(++i; i<arr.length; i++) {
					query.append(delimiter);
					fn.accept(arr[i]);
				}
			}
		}
		else if(idx > arr.length) {
			throw new IndexOutOfBoundsException(idx);
		}// idx == arr.length 
		return this;
	}

	@Override
	public String toString() {
		return query.toString();
	}

	public static QueryBuilder addWithValue() {
		return create(NO_ENV, null, null, null, null);
	}

	public static QueryBuilder addWithValue(Environment environment, QueryView[] ctes, DBView[] views, Map<DBView, QueryView> overview) {
		return create(environment, ctes, views, null, overview);
	}

	public static QueryBuilder parameterized(Environment environment, QueryView[] ctes, DBView[] views, Map<DBView, QueryView> overview) {
		return create(environment, ctes, views, new ArrayList<>(), overview);
	}
	
	private static QueryBuilder create(Environment environment, QueryView[] ctes, DBView[] views, List<TypedArg> args, Map<DBView, QueryView> overview) {
		var cMap = viewAlias("g", ctes);
		var vMap = viewAlias("v", views);
		if(!isEmpty(overview)) {
			overview.forEach((k,v)-> vMap.put(k, cMap.get(v))); //override or add
		}
		overview = nonNull(overview) ? overview : emptyMap();
		return new QueryBuilder(environment, "v", new StringBuilder(), unmodifiableMap(cMap), unmodifiableMap(vMap), args, overview, null);
	}
		
	private static <T> Map<T, String> viewAlias(String prefix, T[] views){
		var map = new LinkedHashMap<T, String>(); //preserve order
		if(!isEmpty(views) && nonNull(prefix)) {
			int i=0;
			for(var v : views) {
				map.put(v, prefix+i++);
			}
		}
		return map;
	}
}

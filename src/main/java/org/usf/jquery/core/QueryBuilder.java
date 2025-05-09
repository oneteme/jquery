package org.usf.jquery.core;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.usf.jquery.core.JDBCType.typeOf;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.core.TypedArg.arg;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.usf.jquery.core.Driven.Adjuster;

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
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryBuilder {
	
	@Getter
	private final String schema;
	private final String prefix;
	private final StringBuilder query;
	private final Map<QueryView, String> ctes;
	private final Map<DBView, String> views;
	private final List<TypedArg> args;
	private final Object currentModel;
	
	public QueryBuilder appendViewAlias(DBView view) {
		return appendViewAlias(view, "");
	}
	
	public QueryBuilder appendViewAlias(DBView view, String after) {
		if(!ctes.isEmpty() || !views.isEmpty()) {
			var v = ctes.containsKey(view) ? ctes.get(view) : views.get(view);
			if(nonNull(v)) {
				append(v).append(after); //view.
			}
			else {
				log.warn("alias not found for view=" + view);
			}
		} //else no alias
		return this;
	}
	
	public QueryBuilder append(String sql, Adjuster<String> adj) {
		return append(nonNull(adj) 
				? adj.adjust(sql, requireNonNull(currentModel, "currentModel is null")) 
				: sql);
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
	
	public QueryBuilder appendParameter(Object o, Adjuster<Object> adj) {
		return appendParameter(nonNull(adj) 
				? adj.adjust(o, requireNonNull(currentModel, "currentModel is null")) 
				: o);
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
	
	public QueryBuilder appendEach(String delimiter, Collection<? extends DBObject> it) {
		return runForeach(delimiter, it, o-> o.build(this));
	}
	
	public <T> QueryBuilder appendEach(String delimiter, Collection<T> it, Consumer<T> cons) {
		return runForeach(delimiter, it, cons);
	}

	public QueryBuilder withValue() { //inherit schema, prefix, args but not views
		return new QueryBuilder(schema, prefix, query, ctes, views, null, currentModel); //no args
	}

	public QueryBuilder withModel(Object model) { //overwrite model only
		return new QueryBuilder(schema, prefix, query, ctes, views, args, model);
	}
	
	public QueryBuilder subQuery(Collection<DBView> views) { //inherit schema, prefix, args but not views
		var s = prefix + "_s";
		return new QueryBuilder(schema, s, query, ctes, viewAlias(s, views), args, currentModel);
	}
	
	public Query build() {
		return new Query(query.toString(), isParameterized() ? args.toArray(TypedArg[]::new) : null);
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

	private <T> QueryBuilder runForeach(String delimiter, Collection<T> c, Consumer<T> cons) {
		if(!isEmpty(c)) {
			var it = c.iterator();
			cons.accept(it.next());
			while(it.hasNext()) {
				query.append(delimiter);
				cons.accept(it.next());
			}
		} 
		return this;
	}

	@Override
	public String toString() {
		return query.toString();
	}

	public static QueryBuilder addWithValue() {
		return create(null, emptyList(), emptyList(), null, null);
	}

	public static QueryBuilder addWithValue(String schema, Collection<QueryView> ctes, Collection<DBView> views, Map<DBView, QueryView> overview) {
		return create(schema, ctes, views, null, overview);
	}

	public static QueryBuilder parameterized(String schema, Collection<QueryView> ctes, Collection<DBView> views, Map<DBView, QueryView> overview) {
		return create(schema, ctes, views, new ArrayList<>(), overview);
	}
	
	private static QueryBuilder create(String schema, Collection<QueryView> ctes, Collection<DBView> views, List<TypedArg> args, Map<DBView, QueryView> overview) {
		var cMap = viewAlias("g", ctes);
		var vMap = viewAlias("v", views);
		if(!isEmpty(overview)) {
			overview.forEach((k,v)-> vMap.put(k, cMap.get(v))); //override or add
		}
		return new QueryBuilder(schema, "v", new StringBuilder(), unmodifiableMap(cMap), unmodifiableMap(vMap), args, null);
	}
		
	private static <T> Map<T, String> viewAlias(String prefix, Collection<T> views){
		var map = new HashMap<T, String>();
		if(!isEmpty(views) && nonNull(prefix)) {
			int i=0;
			for(var v : views) {
				map.put(v, prefix+i++);
			}
		}
		return map;
	}
}

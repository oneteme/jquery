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
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryBuilder {
	
	private static final String P_ARG = "?";
	
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
		if(ctes.isEmpty() && views.isEmpty()) {
			return this; //no alias
		}
		var v = ctes.containsKey(view) ? ctes.get(view) : views.get(view);
		if(nonNull(v)) {
			return append(v).append(after); //view.
		}
		throw new NoSuchElementException("no alias for view " + view);
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
	
	public QueryBuilder append(String sql) {
		query.append(sql);
		return this;
	}
	
	public QueryBuilder append(DBObject o) {
		o.build(this);
		return this;
	}
	
	public QueryBuilder appendParameters(String delemiter, Object[] arr) {
		return appendParameters(delemiter, arr, 0);
	}
	
	public QueryBuilder appendParameters(String delemiter, Object[] arr, int from) {
		return isParameterized()
			? runForeach(delemiter, arr, from, this::appendParameter)
			: appendLiteral(delemiter, arr, from);
	}
	
	public QueryBuilder appendEach(String delemiter, DBObject[] arr) {
		return runForeach(delemiter, arr, 0, o-> o.build(this));
	}
	
	public <T> QueryBuilder appendEach(String delemiter, T[] arr, Consumer<T> cons) {
		return runForeach(delemiter, arr, 0, cons);
	}
	
	public QueryBuilder appendEach(String delemiter, Collection<? extends DBObject> it) {
		return runForeach(delemiter, it, o-> o.build(this));
	}
	
	public <T> QueryBuilder appendEach(String delemiter, Collection<T> it, Consumer<T> cons) {
		return runForeach(delemiter, it, cons);
	}

	public QueryBuilder appendLiteral(String delemiter, Object[] arr) {
		return appendLiteral(delemiter, arr, 0);
	}
	
	public QueryBuilder appendLiteral(String delemiter, Object[] arr, int from) {
		return runForeach(delemiter, arr, from, this::appendLiteral);
	}

	public QueryBuilder appendParameter(Object o) {
		if(isParameterized()) {
			return o instanceof DBObject jo
					? append(jo)
					: append(typeOf(o).map(t-> appendArg(t, o)).orElseGet(()-> formatValue(o)));
		}
		else {
			return appendLiteral(o);
		}
	}

	public QueryBuilder appendLiteral(Object o) {  //TD : stringify value using db default pattern
		return o instanceof DBObject jo 
				? append(jo) 
				: append(formatValue(o));
	}
		
	private String appendArg(JDBCType type, Object o) {
		args.add(arg(o, type.getValue()));
		return P_ARG;
	}
	
	private boolean isParameterized() {
		return nonNull(args);
	}

	public static String formatValue(Object o) {
		if(o instanceof Number){
			return o.toString();
		}//else String|Date
		return nonNull(o) ? quote(o.toString()) : "null";
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
		return create(null, emptyList(), emptyList(), emptyList(), null);
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
			overview.forEach((k,v)-> vMap.put(k, cMap.get(v)));//override or add
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

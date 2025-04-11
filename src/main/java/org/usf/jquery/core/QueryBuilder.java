package org.usf.jquery.core;

import static java.util.Collections.emptyList;
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
import java.util.Iterator;
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
	
	public QueryBuilder appendSpace() {
		return append(SPACE);
	}
	
	public QueryBuilder appendAs() {
		return append(" AS ");
	}
	
	public QueryBuilder appendViewAlias(DBView view) {
		var v = ctes.containsKey(view) ? ctes.get(view) : views.get(view);
		if(nonNull(v)) {
			return append(v);
		}
		throw new NoSuchElementException("no alias for view " + view);
	}
	
	public QueryBuilder appendParenthesis(Runnable exec) {
		append("(");
		exec.run();
		append(")");
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
	
	public void appendParameters(String delemiter, Object[] arr) {
		appendParameters(delemiter, arr, 0);
	}
	
	public void appendParameters(String delemiter, Object[] arr, int from) {
		if(dynamic()) {
			runForeach(delemiter, arr, from, this::appendParameter);
		}
		else {
			appendLiteral(delemiter, arr, from);
		}
	}
	
	public QueryBuilder append(String delemiter, DBObject[] arr) {
		return runForeach(delemiter, arr, 0, o-> o.build(this));
	}
	
	public <T> QueryBuilder append(String delemiter, T[] arr, Consumer<T> cons) {
		return runForeach(delemiter, arr, 0, cons);
	}
	
	public QueryBuilder append(String delemiter, Iterator<? extends DBObject> it) {
		return runForeach(delemiter, it, o-> o.build(this));
	}
	
	public <T> QueryBuilder append(String delemiter, Iterator<T> it, Consumer<T> cons) {
		return runForeach(delemiter, it, cons);
	}

	public QueryBuilder appendLiteral(String delemiter, Object[] arr) {
		return appendLiteral(delemiter, arr, 0);
	}
	
	public QueryBuilder appendLiteral(String delemiter, Object[] arr, int from) {
		return runForeach(delemiter, arr, from, this::appendLiteral);
	}

	public QueryBuilder appendParameter(Object o) {
		if(dynamic()) {
			if(o instanceof DBObject jo) {
				jo.build(this);
			}
			else {
				var t = typeOf(o);
				query.append(t.isPresent() ? appendArg(t.get(), o) : formatValue(o));
			}
		}
		else {
			appendLiteral(o);
		}
		return this;
	}

	public QueryBuilder appendLiteral(Object o) {  //TD : stringify value using db default pattern
		if(o instanceof DBObject jo) {
			jo.build(this);
		}
		else {
			append(formatValue(o));
		}
		return this;
	}
		
	private String appendArg(JDBCType type, Object o) {
		args.add(arg(o, type.getValue()));
		return P_ARG;
	}
	
	private boolean dynamic() {
		return nonNull(args);
	}

	public static String formatValue(Object o) {
		if(o instanceof Number){
			return o.toString();
		}//else String|Date
		return nonNull(o) ? quote(o.toString()) : "null";
	}
	
	public QueryBuilder withValue() {
		return new QueryBuilder(schema, prefix, query, ctes, views, null); //no args
	}
	
	public QueryBuilder subQuery(Collection<DBView> views) { //share schema, prefix, args but not views
		var s = prefix + "_s";
		return new QueryBuilder(schema, s, query, ctes, toLinkedMap(s, views), args);
	}
	
	public Query build() {
		return new Query(query.toString(), dynamic() ? args.toArray(TypedArg[]::new) : null);
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

	private <T> QueryBuilder runForeach(String delimiter, Iterator<T> it, Consumer<T> cons) {
		if(nonNull(it) && it.hasNext()) {
			cons.accept(it.next());
			while(it.hasNext()) {
				query.append(delimiter);
				cons.accept(it.next());
			}
		} 
		return this;
	}

	public static QueryBuilder addWithValue() {
		return addWithValue(null, emptyList(), emptyList()); //no args
	}

	public static QueryBuilder addWithValue(String schema, Collection<QueryView> ctes, Collection<DBView> views) {
		return new QueryBuilder(schema, "v", new StringBuilder(), toLinkedMap("g", ctes), toLinkedMap("v", views), null);
	}

	public static QueryBuilder parameterized(String schema, Collection<QueryView> ctes, Collection<DBView> views) {
		return new QueryBuilder(schema, "v", new StringBuilder(), toLinkedMap("g", ctes), toLinkedMap("v", views), new ArrayList<>());
	}
		
	private static <T> Map<T, String> toLinkedMap(String prefix, Collection<T> views){
		var map = new HashMap<T, String>();
		if(!isEmpty(views)) {
			int i=0;
			for(var v : views) {
				map.put(v, prefix+i);
			}
		}
		return map; //modifiable map
	}	
}

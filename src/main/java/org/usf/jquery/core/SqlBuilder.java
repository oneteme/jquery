package org.usf.jquery.core;

import static java.util.Collections.emptyMap;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@Getter(AccessLevel.PACKAGE)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SqlBuilder {

	@Getter
	private final Store store;
	private final StringBuilder query;
	private final Map<Query, String> ctes;
	private final Map<View, String> views;
	private final Map<Column, String> columns;
	private final List<TypedArg> args;
	private final Map<View, Query> overViews;
	@Setter
	private boolean useReference;

	public boolean isCte(View view) {
		return ctes.containsKey(view) || overViews.containsKey(view);
	}

	public SqlBuilder appendViewAlias(View view) {
		return appendViewAlias(view, "");
	}

	public SqlBuilder appendViewAlias(View view, String after) {
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

	public SqlBuilder appendSpace() {
		return append(SPACE);
	}

	public SqlBuilder appendAs() {
		return append(" AS ");
	}

	public SqlBuilder append(String v) {
		query.append(v);
		return this;
	}

	public SqlBuilder append(int n) {
		query.append(n);
		return this;
	}

	public SqlBuilder append(char c) {
		query.append(c);
		return this;
	}

	public SqlBuilder append(QueryPart o) {
		o.build(this);
		return this;
	}

	public SqlBuilder appendParameter(Object o) {
		return appendParameter(o, false);
	}

	public SqlBuilder appendParameter(Object o, boolean parameterized) { //comparators
		if(o instanceof QueryPart jo) {
			return append(jo);
		}
		if(parameterized && isParameterized()) {
			var t = typeOf(o);
			if(t.isPresent()) {
				args.add(arg(o, t.get().getValue()));
				return append("?");
			}
		}
		if(o instanceof Number || o instanceof Boolean){
			return append(o.toString());
		} 
		return append(nonNull(o) ? quote(o.toString()) : "null");   //TD : format value using dialect if possible
	}
	
	public SqlBuilder appendParameters(String delimiter, Collection<?> arr) {
		return appendEach(delimiter, arr, v-> appendParameter(v, false));
	}

	public SqlBuilder appendParameters(String delimiter, Collection<?> arr, boolean parameterized) {
		return appendEach(delimiter, arr, v-> appendParameter(v, parameterized));
	}

	public SqlBuilder appendEach(String delimiter, Collection<? extends QueryPart> arr) {
		return appendEach(delimiter, arr, this::append);
	}

	<T> SqlBuilder appendEach(String delimiter, Collection<T> arr, Consumer<T> cons) {
		if(!isEmpty(arr)) {
			var it = arr.iterator();
			cons.accept(it.next());
			while(it.hasNext()) {
				query.append(delimiter);
				cons.accept(it.next());
			}
		}
		return this;
	}

	public SqlBuilder withValue() { //inherit schema, prefix, views but not args
		return new SqlBuilder(store, query, ctes, views, columns, null, overViews);
	}

	public SqlBuilder subQuery(Query query) {
//		var s = prefix + "_s";
//		var vMap = viewAlias(s, views);
//		if(!isEmpty(overview)) {
//			overview.forEach((k,v)-> vMap.put(k, ctes.get(v))); //override or add
//		}
//		return new SqlBuilder(store, s, query, ctes, vMap, args, overview);
		return null;
	}

	private boolean isParameterized() {
		return nonNull(args);
	}

	public String aliasFor(Column sql) {
		return useReference && store.dialect().supportAliasReference() ? columns.get(sql) : null;
	}

	public SqlQuery build() {
		return new SqlQuery(store, query.toString(), isParameterized() ? args.toArray(TypedArg[]::new) : null);
	}

	@Override
	public String toString() {
		return query.toString();
	}

	static SqlBuilder addWithValue(Store store) {
		return new SqlBuilder(store, new StringBuilder(), emptyMap(), emptyMap(), emptyMap(), null, emptyMap());
	}

	static SqlBuilder create(Query query, boolean parameterized) {
		var cMap = viewAlias("g", query.getCtes());
		var vMap = viewAlias("v", query.getFroms());
		var sMap = columnAlias("unnamed_", query.getSelects());
		if(!isEmpty(query.getJoins())) {
			for(var j : query.getJoins()) {  //add join views to alias map
				vMap.put(j.getView(), "v"+ (1+vMap.size()));
			}
		}
		Map<View, Query> ovr = emptyMap();
		if(!isEmpty(query.getOverView())) {
			ovr = unmodifiableMap(query.getOverView());
			for(var e : ovr.entrySet()) {
				vMap.put(e.getKey(), cMap.get(e.getValue()));
			}
		}
		var args = parameterized ? new ArrayList<TypedArg>() : null;
		return new SqlBuilder(query.getStore(), new StringBuilder(), cMap, vMap, sMap, args, ovr);
	}

	static <T> Map<T, String> viewAlias(String prefix, Collection<T> views){
		if(!isEmpty(views)) {
			var map = new LinkedHashMap<T, String>(); //preserve order
			var i = 0;
			for(var v : views) {
				map.put(v, prefix+ ++i);
			}
			return map;
		}
		return emptyMap();
	}

	static Map<Column, String> columnAlias(String prefix, Collection<Column> columns){
		if(!isEmpty(columns)) {
			var map = new LinkedHashMap<Column, String>(); //preserve order
			var i = 0;
			for(var c : columns) {
				map.put(c, nonNull(c.getTag()) ? c.getTag() : prefix + ++i);
			}
			return map;
		}
		return emptyMap();
	}
}

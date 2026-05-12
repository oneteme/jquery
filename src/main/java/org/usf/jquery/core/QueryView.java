package org.usf.jquery.core;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.nonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toSet;
import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.QueryBuilder.addWithValue;
import static org.usf.jquery.core.QueryBuilder.parameterized;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

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
@Getter
@Setter(value = AccessLevel.PACKAGE)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class QueryView implements DBView {

	private final Store store;
	
	private QueryView[] ctes;
	private Column[] selects;
	private Column[] groups;
	private Criteria[] wheres; 
	private Criteria[] havings;
	private ViewJoin[] joins; 
	private Order[] orders;
	private DBView[] froms; //preserve order
	private QueryUnion[] unions;
	private boolean distinct;
	private boolean aggregation;
	private int limit = -1;
	private int offset = -1;
	private Map<DBView, QueryView> overView = emptyMap();
	
	@Override
	public int prepare(QueryManifest manifest) {
		if(!isEmpty(ctes)) { // subQuery
			for(var c : ctes) {
				manifest.cte(c);
			}
		}
		return SCALAR;
	}

	@Override
	public void build(QueryBuilder builder) {
		var ovr = builder.isCte(this) ? overView : assign(builder.getOverViews(), overView); //TODO merge 
		var sub = builder.subQuery(froms, unmodifiableMap(ovr));
		sub.appendParenthesis(()-> buildClauses(sub));
	}

	public Query build() {
		return buildQuery(true);
	}
	
	public Query buildQuery(boolean parameterized) {
		log.trace("building query...");
		var bg = currentTimeMillis();
		var flatCTE = flatCte().distinct().toArray(QueryView[]::new);
		Map<DBView, QueryView> over = isEmpty(overView) ? emptyMap() : unmodifiableMap(overView);
		var builder = parameterized 
				? parameterized(store, flatCTE, froms, over)
				: addWithValue(store, flatCTE, froms, unmodifiableMap(overView));
		if(!isEmpty(flatCTE)) {
			builder.append("WITH ") // do not resolveView => ViewRef
			.appendEach(SCOMA, flatCTE, v-> builder.appendViewAlias(v).appendAs().append(v)) 
			.appendSpace();
		}
		buildClauses(builder);
		log.trace("query built in {} ms", currentTimeMillis() - bg);
		return builder.build();
	}
	
	private void buildClauses(QueryBuilder builder) {
		select(builder);
		from(builder);
		join(builder);
    	where(builder);
    	groupBy(builder);
    	having(builder);
    	orderBy(builder);
    	pagination(builder);
    	union(builder);
	}

	void select(QueryBuilder builder){
		builder.append("SELECT ");
		if(distinct) {
			builder.append("DISTINCT ");
		}
    	if(limit > -1 && store.dialect().supportTopClause()){
    		builder.append("TOP " + limit);
    	}
    	builder.appendEach(SCOMA, selects, o-> {
    		builder.append(o);
    		if(o instanceof NamedColumn nc && nonNull(nc.getTag())) {
    			builder.appendAs().append(doubleQuote(nc.getTag()));
    		}
    	});
	}
	
	void from(QueryBuilder query) {
		if(!isEmpty(froms)) {
			var from = Stream.of(froms).map(v-> overView.containsKey(v) ? overView.get(v) : v).collect(toSet());
			if(!isEmpty(joins)) {
				Stream.of(joins) //exclude join views
				.map(ViewJoin::getView)
				.forEach(v-> from.remove(overView.containsKey(v) ? overView.get(v) : v));
			}
			if(!from.isEmpty()) {
				query.append(" FROM ").appendEach(SCOMA, from.toArray(DBView[]::new), v-> {
					var res = v.resolveView(query);
					query.append(res).appendSpace().appendViewAlias(res);
				});
			}
		}
	}
	
	void join(QueryBuilder builder) {
		if(!isEmpty(joins)) {
			builder.appendSpace().appendEach(SPACE, joins);
		}
	}

	void where(QueryBuilder builder){
		if(!isEmpty(wheres)) {
    		builder.append(" WHERE ").appendEach(AND.sql(), wheres);
		}
	}
	
	void groupBy(QueryBuilder builder){
		if(!isEmpty(groups)) {
			Consumer<Column> cons = builder::append;
			if(store.dialect().supportGroupByIndex()) {
				cons = appendEachByIndex(builder, identity());
			}
			else if(store.dialect().supportGroupByAlias()) {
				cons = appendEachByRef(builder, identity());
			}
    		builder.append(" GROUP BY ").appendEach(SCOMA, groups, cons);
		}
	}

	void having(QueryBuilder builder){
		if(!isEmpty(havings)) {
    		builder.append(" HAVING ").appendEach(AND.sql(), havings, appendEachByIndex(builder, Function.identity()));
		}
	}
	
	void orderBy(QueryBuilder builder) {
    	if(!isEmpty(orders)) {
    		builder.append(" ORDER BY ").appendEach(SCOMA, orders, appendEachByIndex(builder, Order::getColumn));
    	}
	}
	
	void pagination(QueryBuilder builder) {
		if(limit > -1) {
			if(store.dialect().supportLimitClause()) {
				builder.append(" LIMIT ").append(limit);
			}
			else if(store.dialect().supportFetchClause()) {
				builder.append(" FETCH NEXT ").append(limit).append(" ROWS ONLY");
			}
			else if(!store.dialect().supportTopClause()) {
				throw new UnsupportedOperationException("limit="+limit);
			}
		}
		if(offset > -1) {
			if(store.dialect().supportOffsetClause()) {
				builder.append(" OFFSET ").append(offset); //.append(" ROWS")  //.append(" ROWS ONLY") H2 not support it
			}
			else {
				throw new UnsupportedOperationException("offset="+limit);
			}
		}
	}

	void union(QueryBuilder builder) {
    	if(!isEmpty(unions)) {
    		builder.appendSpace().appendEach(SPACE, unions);
    	}
	}

	public SingleQueryColumn asColumn(){ 
		return new SingleQueryColumn(this); 
	}
	
	public QueryUnion asUnion(boolean all) {
		return new QueryUnion(all, this);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this); 
	}
	
	Stream<QueryView> flatCte(){
		return isEmpty(ctes)
				? Stream.empty()
				: Stream.concat(Stream.of(ctes).flatMap(QueryView::flatCte), Stream.of(ctes));
	}

	<T extends DBObject> Consumer<T> appendEachByIndex(QueryBuilder builder, Function<T, Column> fn){
		var cols = List.of(selects);
		return o-> {
			var idx = cols.indexOf(fn.apply(o));
			if(idx > -1) {
				builder.append(idx+1); 
			}
			else {
				builder.append(o);
			}
		};
	}
	
	<T extends DBObject> Consumer<T> appendEachByRef(QueryBuilder builder, Function<T, Column> fn){
		var cols = List.of(selects);
		return o-> {
			var c = fn.apply(o);
			if(c instanceof NamedColumn nc && cols.contains(c)) {
				builder.append(doubleQuote(nc.getTag()));
			}
			else {
				builder.append(o);
			}
		};
	}

	static <K,V> Map<K,V> assign(Map<K,V> m1, Map<K,V> m2){
		if(!isEmpty(m1) && !isEmpty(m2)) {
			var map = new HashMap<>(m1);
			map.putAll(m2);
			return map;
		}
		if(isEmpty(m1)) {
			return m2;
		}
		return isEmpty(m2) ? m1 : emptyMap();
	}
}
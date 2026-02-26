package org.usf.jquery.core;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;
import static org.usf.jquery.core.Environment.NO_ENV;
import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.DatabaseVendor.TERADATA;
import static org.usf.jquery.core.QueryBuilder.addWithValue;
import static org.usf.jquery.core.QueryBuilder.parameterized;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
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
public final class QueryView implements DBView {

	private QueryView[] ctes;
	private NamedColumn[] columns;
	private DBColumn[] group; 
	private DBFilter[] where; 
	private DBFilter[] having;
	private ViewJoin[] joins; 
	private DBOrder[] orders;
	private DBView[] views; //preserve order
	private QueryUnion[] unions;
	private boolean distinct;
	private boolean aggregation;
	private Integer limit;
	private Integer offset;
	private Map<DBView, QueryView> overView;
	
	@Override
	public int compose(QueryComposer composer, Consumer<DBColumn> groupKeys) {
		if(!isEmpty(ctes)) { // subQuery
			composer.ctes(ctes);
		}
		return -1;
	}

	@Override
	public void build(QueryBuilder query) {
		var ovr = query.isCte(this) ? overView : assign(query.getOverViews(), overView);
		var sub = query.subQuery(views, unmodifiableMap(ovr));
		sub.appendParenthesis(()-> buildClauses(sub));
	}

	public Query build() {
		return buildQuery(NO_ENV, true);
	}
	
	public Query buildQuery(Environment env, boolean parameterized, Object... drivenModel) {
		log.trace("building query...");
		var bg = currentTimeMillis();
		var flatCTE = flatCte().distinct().toArray(QueryView[]::new);
		var builder = parameterized 
				? parameterized(env, flatCTE, views, unmodifiableMap(overView))
				: addWithValue(env, flatCTE, views, unmodifiableMap(overView));
		if(!isEmpty(flatCTE)) {
			builder.append("WITH ") // do not resolveView => ViewRef
			.appendEach(SCOMA, flatCTE, v-> builder.appendViewAlias(v).appendAs().append(v)) 
			.appendSpace();
		}
		if(isEmpty(drivenModel)) {
			buildClauses(builder);
		}
		else {
			builder.appendEach(" UNION ALL ", drivenModel, o-> buildClauses(builder.withModel(o)));
		}
		log.trace("query built in {} ms", currentTimeMillis() - bg);
		return builder.build(env);
	}
	
	private void buildClauses(QueryBuilder builder) {
		select(builder);
		from(builder);
		join(builder);
    	where(builder);
    	groupBy(builder);
    	having(builder);
    	orderBy(builder);
    	fetch(builder);
    	union(builder);
	}

	void select(QueryBuilder builder){
		builder.append("SELECT ");
		if(distinct) {
			builder.append("DISTINCT ");
		}
		if(builder.getEnvironment().getProduct() == TERADATA) {
			if(nonNull(offset)) {
				throw new UnsupportedOperationException("OFFSET option is not supported in Teradata.");
			}
	    	if(nonNull(limit)){
				if(distinct) {
					throw new UnsupportedOperationException("Top N option is not supported with DISTINCT option.");
				}
	    		builder.append("TOP " + limit);
	    	}
		}
    	builder.appendEach(SCOMA, columns, o-> {
    		builder.append(o);
    		var tag = o.getTag();
    		if(nonNull(tag)) {
    			builder.appendAs().append(doubleQuote(tag));
    		}
    	});
	}
	
	void from(QueryBuilder query) {
		if(!isEmpty(views)) {
			var from = Stream.of(views).map(v-> overView.containsKey(v) ? overView.get(v) : v).collect(toSet());
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
		if(!isEmpty(where)) {
    		builder.append(" WHERE ").appendEach(AND.sql(), where);
		}
	}
	
	void groupBy(QueryBuilder builder){
		if(aggregation && !isEmpty(group)) {
			var cols = Stream.of(columns).collect(toSet());
    		builder.append(" GROUP BY ").appendEach(SCOMA, group, c-> {
    			if(!(c instanceof ViewColumn) && cols.contains(c)) {
    				builder.append(((NamedColumn)c).getTag());
    			}
    			else {
    				 c.build(builder);
    			}
    		});
		}
	}

	void having(QueryBuilder builder){
		if(!isEmpty(having)) {
    		builder.append(" HAVING ").appendEach(AND.sql(), having);
		}
	}
	
	void orderBy(QueryBuilder builder) {
    	if(!isEmpty(orders)) {
    		builder.append(" ORDER BY ").appendEach(SCOMA, orders);
    	}
	}
	
	void fetch(QueryBuilder builder) {
		if(builder.getEnvironment().getProduct() != TERADATA) { // TOP n
			if(nonNull(limit)) {
				builder.append(" LIMIT " + limit);
			}
			if(nonNull(offset)) {
				builder.append(" OFFSET " + offset);
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
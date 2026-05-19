package org.usf.jquery.core;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.QueryBuilder.addWithValue;
import static org.usf.jquery.core.QueryBuilder.parameterized;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.Collection;
import java.util.Map;

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
	
	private Collection<QueryView> ctes;
	private Collection<Column> selects;
	private Collection<Column> groups;
	private Collection<Criteria> wheres; 
	private Collection<Criteria> havings;
	private Collection<ViewJoin> joins; 
	private Collection<Order> orders;
	private Collection<DBView> froms; //preserve order
	private Collection<QueryUnion> unions;
	private boolean distinct;
	private boolean aggregation;
	private int limit = -1;
	private int offset = -1;
	private Map<DBView, QueryView> overView;
	
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
		var ovr = builder.isCte(this) ? overView : builder.getOverViews();
		var sub = builder.subQuery(froms, unmodifiableMap(ovr));
		sub.append("(");
		buildClauses(sub);
		sub.append(")");
	}

	public Query build() {
		return buildQuery(true);
	}
	
	public Query buildQuery(boolean parameterized) {
		log.trace("building query...");
		var bg = currentTimeMillis();
		Map<DBView, QueryView> over = isEmpty(overView) ? emptyMap() : unmodifiableMap(overView);
		var builder = parameterized 
				? parameterized(store, ctes, froms, over)
				: addWithValue(store, ctes, froms, unmodifiableMap(overView));
		if(!isEmpty(ctes)) {
			builder.append("WITH ")
			.appendEach(SCOMA, ctes, v-> builder.appendViewAlias(v).appendAs().append(v)) 
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
    	builder.setUseReference(store.dialect().supportAliasReference());
    	groupBy(builder);
    	having(builder);
    	orderBy(builder);
    	pagination(builder);
    	union(builder);
	}

	void select(QueryBuilder builder){
		builder.append("SELECT");
		if(distinct) {
			builder.append(" DISTINCT");
		}
    	if(limit > -1 && store.dialect().supportTopClause()){
    		builder.append(" TOP ").append(limit);
    	}
    	var map = builder.getColumnMap();
    	builder.append(SPACE).appendEach(SCOMA, map.entrySet(), o-> {
    		builder.append(o.getKey());
    		if(nonNull(o.getValue())) { //e.g AsteriskColumn
    			builder.appendAs().append(o.getValue());
    		}
    	});
	}
	
	void from(QueryBuilder builder) {
		if(!isEmpty(froms)) {
			builder.append(" FROM ").appendEach(SCOMA, froms, v-> {
				if(!builder.isCte(v)) {
					builder.append(v).appendSpace();
				}
				builder.appendViewAlias(v);
			});
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
			builder.append(" GROUP BY ").appendEach(SCOMA, groups);
		}
	}

	void having(QueryBuilder builder){
		if(!isEmpty(havings)) {
    		builder.append(" HAVING ").appendEach(AND.sql(), havings);
		}
	}
	
	void orderBy(QueryBuilder builder) {
    	if(!isEmpty(orders)) {
    		builder.append(" ORDER BY ").appendEach(SCOMA, orders);
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
}
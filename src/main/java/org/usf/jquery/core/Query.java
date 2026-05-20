package org.usf.jquery.core;

import static java.lang.System.currentTimeMillis;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.SqlBuilder.create;
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
public class Query implements View {

	private final Store store;
	
	private Collection<Query> ctes;
	private Collection<Column> selects;
	private Collection<Column> groups;
	private Collection<Criteria> wheres; 
	private Collection<Criteria> havings;
	private Collection<Join> joins; 
	private Collection<Order> orders;
	private Collection<View> froms; //preserve order
	private Collection<Union> unions;
	private boolean distinct;
	private boolean aggregation;
	private int limit = -1;
	private int offset = -1;
	private Map<View, Query> overView;
	
	@Override
	public int prepare(QueryAnalyzer manifest) {
		if(!isEmpty(ctes)) { // subQuery
			for(var c : ctes) {
				manifest.cte(c);
			}
		}
		return SCALAR;
	}

	@Override
	public void build(SqlBuilder builder) {
		buildClauses(builder.subQuery(this)); //build sub query without ctes
	}

	public SqlQuery build() {
		return buildQuery(true);
	}
	
	public SqlQuery buildQuery(boolean parameterized) {
		log.trace("building query...");
		var bg = currentTimeMillis();
		var builder = create(this, parameterized);
		if(!isEmpty(ctes)) {
			builder.append("WITH ").append('(')
			.appendEach(SCOMA, ctes, v-> builder.appendViewAlias(v).appendAs().append(v)) 
			.append(") ");
		}
		buildClauses(builder);
		log.trace("query built in {} ms", currentTimeMillis() - bg);
		return builder.build();
	}
	
	private void buildClauses(SqlBuilder builder) {
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

	void select(SqlBuilder builder){
		builder.append("SELECT");
		if(distinct) {
			builder.append(" DISTINCT");
		}
    	if(limit > -1 && store.dialect().supportTopClause()){
    		builder.append(" TOP ").append(limit);
    	}
    	var map = builder.getColumns();
    	builder.append(SPACE).appendEach(SCOMA, map.entrySet(), o-> {
    		builder.append(o.getKey());
    		if(nonNull(o.getValue())) { //e.g AsteriskColumn
    			builder.appendAs().append(o.getValue());
    		}
    	});
	}
	
	void from(SqlBuilder builder) {
		if(!isEmpty(froms)) {
			builder.append(" FROM ").appendEach(SCOMA, froms, v-> {
				if(!builder.isCte(v)) {
					builder.append(v).appendSpace();
				}
				builder.appendViewAlias(v);
			});
		}
	}
	
	void join(SqlBuilder builder) {
		if(!isEmpty(joins)) {
			builder.appendSpace().appendEach(SPACE, joins);
		}
	}

	void where(SqlBuilder builder){
		if(!isEmpty(wheres)) {
    		builder.append(" WHERE ").appendEach(AND.sql(), wheres);
		}
	}
	
	void groupBy(SqlBuilder builder){
		if(!isEmpty(groups)) {
			builder.append(" GROUP BY ").appendEach(SCOMA, groups);
		}
	}

	void having(SqlBuilder builder){
		if(!isEmpty(havings)) {
    		builder.append(" HAVING ").appendEach(AND.sql(), havings);
		}
	}
	
	void orderBy(SqlBuilder builder) {
    	if(!isEmpty(orders)) {
    		builder.append(" ORDER BY ").appendEach(SCOMA, orders);
    	}
	}
	
	void pagination(SqlBuilder builder) {
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
				builder.append(" OFFSET ").append(offset);
			}
			else {
				throw new UnsupportedOperationException("offset="+limit);
			}
		}
	}

	void union(SqlBuilder builder) {
    	if(!isEmpty(unions)) {
    		builder.appendSpace().appendEach(SPACE, unions);
    	}
	}

	public SingleQueryColumn asColumn(){ 
		return new SingleQueryColumn(this); 
	}
	
	public Union asUnion(boolean all) {
		//TODO : check if it is a valid union query (e.g same number of columns, compatible types, etc)
		return new Union(all, this);
	}
	
	@Override
	public String toString() {
		return QueryPart.toSQL(this); 
	}
}
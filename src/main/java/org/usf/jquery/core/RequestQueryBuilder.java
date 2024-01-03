package org.usf.jquery.core;

import static java.lang.System.currentTimeMillis;
import static java.util.Objects.isNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.QueryParameterBuilder.parametrized;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;
import static org.usf.jquery.core.Validation.requireNonEmpty;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@Getter
public class RequestQueryBuilder {

	private final List<TaggableColumn> columns = new LinkedList<>();
	private final List<DBFilter> filters = new LinkedList<>();  //WERE & HAVING
	private final List<DBOrder> orders = new LinkedList<>();
	private Iterator<?> it;
	private boolean distinct;
	
	public RequestQueryBuilder distinct() {
		distinct = true;
		return this;
	}

	public RequestQueryBuilder columns(@NonNull TaggableColumn... columns) {
		Stream.of(columns).forEach(this.columns::add);
		return this;
	}

	public RequestQueryBuilder filters(@NonNull DBFilter... filters){
		Stream.of(filters).forEach(this.filters::add);
		return this;
	}
	
	public RequestQueryBuilder orders(@NonNull DBOrder... orders) {
		Stream.of(orders).forEach(this.orders::add);
		return this;
	}
	
	public RequestQueryBuilder repeat(@NonNull Iterator<?> it) {
		this.it = it;
		return this;
	}

	public RequestQuery build(){
		return build(null);
	}

	public RequestQuery build(String schema) {
		log.debug("building query...");
//		requireNonEmpty(tables);
    	requireNonEmpty(columns);
		var bg = currentTimeMillis();
		var pb = parametrized();
		var sb = new SqlStringBuilder(1000); //avg
//		pb.tables(tables.stream().map(TaggableView::tagname).toArray(String[]::new));
		if(isNull(it)) {
			build(sb, pb, schema);
		}
		else {
			sb.forEach(it, " UNION ALL ", o-> build(sb, pb, schema));
		}
		log.debug("query built in {} ms", currentTimeMillis() - bg);
		return new RequestQuery(sb.toString(), pb.args());
	}

	public final void build(SqlStringBuilder sb, QueryParameterBuilder pb, String schema){
    	select(sb, pb, schema);
    	where(sb, pb);
    	groupBy(sb);
    	having(sb, pb);
    	orderBy(sb, pb);
	}

	void select(SqlStringBuilder sb, QueryParameterBuilder pb, String schema){
		sb.append("SELECT ")
    	.appendIf(distinct, ()-> "DISTINCT ")
    	.appendEach(columns, SCOMA, o-> o.sql(pb) + " AS " + doubleQuote(o.tagname()))
    	.append(" FROM ")
    	.appendEach(pb.views(), SCOMA, o-> o.sql(pb, schema, true));
	}

	void where(SqlStringBuilder sb, QueryParameterBuilder pb){
		var expr = filters.stream()
				.filter(not(DBFilter::isAggregation))
				.map(f-> f.sql(pb))
    			.collect(joining(AND.sql()));
    	if(!expr.isEmpty()) {
    		sb.append(" WHERE ").append(expr);
    	}
	}
	
	void groupBy(SqlStringBuilder sb){
        if(isAggregation()) {
        	var expr = columns.stream()
        			.filter(RequestQueryBuilder::groupable)
        			.map(TaggableColumn::tagname) //add alias 
        			.map(SqlStringBuilder::doubleQuote) //sql ??
        			.collect(joining(SCOMA));
        	if(!expr.isEmpty()) {
        		sb.append(" GROUP BY ").append(expr);
        	}
        	else if(columns.size() > 1) {
        		//throw new RuntimeException("require groupBy columns"); CONST !?
        	}
        }
	}
	
	void having(SqlStringBuilder sb, QueryParameterBuilder pb){
		var having = filters.stream()
				.filter(DBFilter::isAggregation)
				.collect(toList());
    	if(!having.isEmpty()) {
    		sb.append(" HAVING ")
    		.appendEach(having, AND.sql(), f-> f.sql(pb));
    	}
	}
	
	void orderBy(SqlStringBuilder sb, QueryParameterBuilder pb) {
    	if(!orders.isEmpty()) {
    		sb.append(" ORDER BY ")
    		.appendEach(orders, SCOMA, o-> o.sql(pb));
    	}
	}
	
	public boolean isAggregation() {
		return columns.stream().anyMatch(DBColumn::isAggregation) ||
				filters.stream().anyMatch(DBFilter::isAggregation);
	}

	private static boolean groupable(DBColumn column) {
		return !column.isAggregation() && !column.isConstant();
	}
}

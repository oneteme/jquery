package org.usf.jquery.core;

import static java.lang.System.currentTimeMillis;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.QueryParameterBuilder.parametrized;
import static org.usf.jquery.core.SqlStringBuilder.EMPTY;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;
import static org.usf.jquery.core.Validation.requireNonEmpty;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
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
public class RequestQuery {

	final List<TaggableColumn> columns = new LinkedList<>();
	final List<TaggableView> tables = new LinkedList<>();
	final List<DBFilter> filters = new LinkedList<>();  //WERE & HAVING
	final List<DBOrder> orders = new LinkedList<>();
	boolean distinct;
	boolean noResult;
	
	public RequestQuery select(TaggableView table, TaggableColumn... columns) {
		return tables(table).columns(columns);
	}
	
	public RequestQuery distinct() {
		distinct = true;
		return this;
	}

	public RequestQuery tables(@NonNull TaggableView... tables) {
		Stream.of(tables).forEach(this.tables::add);
		return this;
	}
	
	public RequestQuery tablesIfAbsent(@NonNull TaggableView... tables) {
		Stream.of(tables)
		.filter(t-> this.tables.stream().noneMatch(tt-> tt.reference().equals(t.reference())))
		.forEach(this.tables::add);
		return this;
	}
	
	public RequestQuery columns(@NonNull TaggableColumn... columns) {
		Stream.of(columns).forEach(this.columns::add);
		return this;
	}

	public RequestQuery filters(@NonNull DBFilter... filters){
		Stream.of(filters).forEach(this.filters::add);
		return this;
	}
	
	public RequestQuery orders(@NonNull DBOrder... orders) {
		Stream.of(orders).forEach(this.orders::add);
		return this;
	}

	public <T> T execute(@NonNull Function<ParametredQuery, T> fn) {
		return fn.apply(build());
	}

	public ParametredQuery build(){
		requireNonEmpty(tables);
    	requireNonEmpty(columns);
		log.debug("building query...");
		var bg = currentTimeMillis();
		var pb = parametrized();
		var sb = new SqlStringBuilder(500);
		build(sb, pb);
		log.debug("query built in {} ms", currentTimeMillis() - bg);
		return new ParametredQuery(sb.toString(), pb.args(), noResult);
	}

	public final void build(SqlStringBuilder sb, QueryParameterBuilder pb){
		pb.tables(tables.stream().map(TaggableView::reference).toArray(String[]::new));
    	select(sb, pb);
    	where(sb, pb);
    	groupBy(sb);
    	having(sb, pb);
    	orderBy(sb, pb);
	}

	void select(SqlStringBuilder sb, QueryParameterBuilder pb){
		sb.append("SELECT ")
    	.appendIf(distinct, ()-> "DISTINCT ")
    	.appendEach(columns, SCOMA, o-> o.sql(pb) + " AS " + doubleQuote(o.reference()))
    	.append(" FROM ")
    	.appendEach(tables, SCOMA, o-> o.sql(pb) + pb.tableAlias(o.reference()).map(v-> SPACE + v).orElse(EMPTY));
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
        			.filter(RequestQuery::groupable)
        			.map(TaggableColumn::reference) //add alias 
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
    		.appendEach(orders, SPACE, o-> o.sql(pb));
    	}
	}
	
	private boolean isAggregation() {
		return columns.stream().anyMatch(DBColumn::isAggregation) ||
				filters.stream().anyMatch(DBFilter::isAggregation);
	}

	private static boolean groupable(DBColumn column) {
		return !column.isAggregation() && !column.isConstant();
	}
}

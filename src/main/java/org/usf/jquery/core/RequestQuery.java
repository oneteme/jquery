package org.usf.jquery.core;

import static java.lang.System.currentTimeMillis;
import static java.lang.reflect.Array.getLength;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;
import static org.usf.jquery.core.QueryParameterBuilder.parametrized;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Validation.requireNonEmpty;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class RequestQuery {

	final List<TaggableTable> tables;
	final List<TaggableColumn> columns;
	final List<DBFilter> filters;  //WERE & HAVING
	final List<OrderColumn> orders;
	boolean distinct;
	boolean noResult;
	
	public RequestQuery() {
		this.tables  = new LinkedList<>();
		this.columns = new LinkedList<>();
		this.filters = new LinkedList<>();
		this.orders  = new LinkedList<>();
	}
	
	public RequestQuery distinct() {
		distinct = true;
		return this;
	}

	public RequestQuery select(TaggableTable table, TaggableColumn... columns) {
		return tables(table).columns(columns);
	}

	public RequestQuery tables(@NonNull TaggableTable... tables) {
		Stream.of(tables).forEach(this.tables::add);
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
	
	public RequestQuery orders(@NonNull OrderColumn... orders) {
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
		String[] cols = columns.stream().map(TaggableColumn::reference).toArray(String[]::new); //!postgres insensitive case
		return new ParametredQuery(sb.toString(), cols, pb.args(), noResult);
	}

	public final void build(SqlStringBuilder sb, QueryParameterBuilder pb){
    	select(sb);
    	where(sb, pb);
    	groupBy(sb);
    	having(sb, pb);
    	orderBy(sb, pb);
	}

	void select(SqlStringBuilder sb){
		var pb = addWithValue(); //addWithValue columns (case, constant, Operation, ..)
    	sb.append("SELECT ")
    	.appendIf(distinct, ()-> "DISTINCT ")
    	.appendEach(columns, SCOMA, o-> o.sql(pb) + " AS " + o.reference())
    	.append(" FROM ")
    	.appendEach(tables, SCOMA, o-> o.sql(pb) + SPACE + o.reference());
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
        if(columns.stream().anyMatch(DBColumn::isAggregation)) {
        	var expr = columns.stream()
        			.filter(RequestQuery::groupable)
        			.map(TaggableColumn::reference) //add alias 
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

	private static boolean groupable(DBColumn column) {
		return !column.isAggregation() && !column.isConstant();
	}
	
	@SuppressWarnings("rawtypes")
	private static int rowCount(Object o) {
		if(o.getClass().isArray()) {
			return getLength(o);
		}
		if(o instanceof Collection) {
			return ((Collection)o).size();
		}
		if(o instanceof Map) {
			return ((Map)o).size();
		}
		return 1; //???
	}
		
}

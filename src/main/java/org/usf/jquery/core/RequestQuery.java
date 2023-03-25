package org.usf.jquery.core;

import static java.lang.System.currentTimeMillis;
import static java.lang.reflect.Array.getLength;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;
import static org.usf.jquery.core.QueryParameterBuilder.parametrized;
import static org.usf.jquery.core.SqlStringBuilder.COMA;
import static org.usf.jquery.core.SqlStringBuilder.POINT;
import static org.usf.jquery.core.Utils.isBlank;
import static org.usf.jquery.core.Validation.requireNonEmpty;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@NoArgsConstructor
public class RequestQuery {

	DBTable table;
	String suffix;
	List<TaggableColumn> columns = new LinkedList<>(); //WERE & HAVING
	List<DBFilter> filters = new LinkedList<>();
	boolean noResult;

	public RequestQuery select(DBTable table, TaggableColumn... columns) {
		return select(table, "", columns);
	}

	public RequestQuery select(DBTable table, String suffix, TaggableColumn... columns) {
		this.table = table;
		this.suffix = suffix;
		return columns(columns);
	}
	
	public RequestQuery columns(TaggableColumn... columns) {
		this.columns.addAll(asList(columns));
		return this;
	}

	public RequestQuery filters(DBFilter... filters){
		this.filters.addAll(asList(filters));
		return this;
	}

	public <T> T execute(Function<ParametredQuery, T> fn) {
		return execute(null, fn);
	}

	public <T> T execute(String schema, Function<ParametredQuery, T> fn) {

		requireNonNull(fn);
		log.debug("Building prepared statement...");
		var bg = currentTimeMillis();
		var query = build(schema);
		log.debug("Query built in {} ms", currentTimeMillis() - bg);
		bg = currentTimeMillis();
		var rows = fn.apply(query);
		log.info("{} rows in {} ms", rowCount(rows), currentTimeMillis() - bg);
		return rows;
	}

	public ParametredQuery build(String schema){
    	requireNonNull(table);
    	requireNonEmpty(columns);
		var pb = parametrized(table);
		var sb = new SqlStringBuilder(500);
		build(schema, sb, pb);
		String[] cols = columns.stream().map(TaggableColumn::reference).toArray(String[]::new); //!postgres insensitive case
		return new ParametredQuery(sb.toString(), cols, pb.args(), noResult);
	}

	public final void build(String schema, SqlStringBuilder sb, QueryParameterBuilder pb){
    	select(schema, sb, pb);
    	where(sb, pb);
    	groupBy(sb, pb);
    	having(sb, pb);
	}

	void select(String schema, SqlStringBuilder sb, QueryParameterBuilder pb){
    	sb.append("SELECT ")
    	.appendEach(columns, COMA, e-> e.tagSql(addWithValue(table))) //addWithValue columns (case, constant, Operation, ..)
    	.append(" FROM ")
    	.appendIf(!isBlank(schema), ()-> schema + POINT)
    	.append(table.sql() + suffix); //TODO call sql with args 
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
	
	void groupBy(SqlStringBuilder sb, QueryParameterBuilder pb){
        if(columns.stream().anyMatch(DBColumn::isAggregation)) {
        	var expr = columns.stream()
        			.filter(RequestQuery::groupable)
        			.map(TaggableColumn::reference) //add alias 
        			.collect(joining(COMA));
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

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
import static org.usf.jquery.core.SqlStringBuilder.POINT;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Utils.isBlank;
import static org.usf.jquery.core.Validation.requireNonEmpty;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public class RequestQuery {

	final String suffix;
	final Set<DBTable> tables;
	final List<TaggableColumn> columns;
	final List<DBFilter> filters;  //WERE & HAVING
	boolean noResult;
	
	public RequestQuery() {
		this.suffix  = null;
		this.tables  = new HashSet<>();
		this.columns = new LinkedList<>();
		this.filters = new LinkedList<>();
	}

	public RequestQuery select(DBTable table, TaggableColumn... columns) {
		return tables(table).columns(columns);
	}

	public RequestQuery tables(@NonNull DBTable... tables) {
		this.tables.addAll(asList(tables));
		return this;
	}
	
	public RequestQuery columns(@NonNull TaggableColumn... columns) {
		this.columns.addAll(asList(columns));
		return this;
	}

	public RequestQuery filters(@NonNull DBFilter... filters){
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
		requireNonEmpty(tables);
    	requireNonEmpty(columns);
		var pb = parametrized();
		var sb = new SqlStringBuilder(500);
		build(schema, sb, pb);
		String[] cols = columns.stream().map(TaggableColumn::reference).toArray(String[]::new); //!postgres insensitive case
		return new ParametredQuery(sb.toString(), cols, pb.args(), noResult);
	}

	public final void build(String schema, SqlStringBuilder sb, QueryParameterBuilder pb){
    	select(schema, sb);
    	where(sb, pb);
    	groupBy(sb);
    	having(sb, pb);
	}

	void select(String schema, SqlStringBuilder sb){
		var pb = addWithValue(); //addWithValue columns (case, constant, Operation, ..)
    	sb.append("SELECT ")
    	.appendEach(columns, SCOMA, o-> o.sql(pb) + " AS " + o.reference())
    	.append(" FROM ")
    	.appendIf(!isBlank(schema), ()-> schema + POINT)
    	.appendEach(tables, SCOMA, o-> o.sql(pb, new Object[]{suffix}) + SPACE + o.reference());
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

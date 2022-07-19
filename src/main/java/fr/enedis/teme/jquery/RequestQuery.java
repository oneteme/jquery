package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.LogicalOperator.AND;
import static fr.enedis.teme.jquery.QueryParameterBuilder.addWithValue;
import static fr.enedis.teme.jquery.QueryParameterBuilder.parametrized;
import static fr.enedis.teme.jquery.SqlStringBuilder.COMA_SEPARATOR;
import static fr.enedis.teme.jquery.SqlStringBuilder.QUOTE_SEPARATOR;
import static fr.enedis.teme.jquery.Utils.isBlank;
import static fr.enedis.teme.jquery.Validation.requireNonEmpty;
import static java.lang.System.currentTimeMillis;
import static java.lang.reflect.Array.getLength;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RequestQuery implements Query {

	String tablename;
	List<TaggableColumn> columns = new LinkedList<>();
	List<DBFilter> filters = new LinkedList<>();
	boolean noResult;

	public RequestQuery select(String tablename, TaggableColumn... columns) {
		this.tablename = tablename;
		return columns(columns);
	}

	public RequestQuery column(boolean condition, Supplier<TaggableColumn> column) {
		if(condition) {
			columns.add(column.get());
		}
		return this;
	}
	public RequestQuery columns(boolean condition, Supplier<TaggableColumn[]> column) {
		return condition ? columns(column.get()) : this;
	}
	public RequestQuery columns(TaggableColumn... columns) {
		this.columns.addAll(asList(columns));
		return this;
	}

	public RequestQuery filter(boolean condition, Supplier<DBFilter> filter){
		if(condition) {
			filters.add(filter.get());
		}
		return this;
	}
	public RequestQuery filters(boolean condition, Supplier<DBFilter[]> filter){
		return condition ? filters(filter.get()) : this;
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

	public final ParametredQuery build(String schema){
		
		var pb = parametrized();
		var sb = new SqlStringBuilder(500);
		build(schema, sb, pb);
		String[] cols = getColumns().stream().map(TaggableColumn::tagname).toArray(String[]::new);
		return new ParametredQuery(sb.toString(), cols, pb.args(), noResult);
	}

	@Override
	public void build(String schema, SqlStringBuilder sb, QueryParameterBuilder pb){
    	
    	requireNonNull(tablename);
    	requireNonEmpty(columns); 
    	sb.append("SELECT ")
    	.appendEach(columns, COMA_SEPARATOR, e-> e.tagSql(addWithValue()))
    	.append(" FROM ")
    	.appendIf(!isBlank(schema), ()-> schema + QUOTE_SEPARATOR)
    	.append(tablename);
    	if(!filters.isEmpty()) {
    		sb.append(" WHERE ")
    		.appendEach(filters, AND.sql(), f-> f.sql(pb));
    	}
        if(columns.stream().anyMatch(DBColumn::isAggregation)) {
        	var gc = columns.stream()
        			.filter(RequestQuery::groupable)
        			.map(TaggableColumn::tagname) //add alias 
        			.collect(toList());
        	if(!gc.isEmpty()) {
        		sb.append(" GROUP BY ").appendEach(gc, COMA_SEPARATOR);
        	}
        	else if(columns.size() > 1) {
        		//throw new RuntimeException("require groupBy columns"); ValueColumn
        	}
        }
	}
	
	public RequestQuery fork(String tn) {
		return new RequestQuery(tn, 
				new LinkedList<>(columns), 
				new LinkedList<>(filters), 
				noResult);
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

package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.JoinType.INNER;
import static fr.enedis.teme.jquery.JoinType.LEFT;
import static fr.enedis.teme.jquery.LogicalOperator.AND;
import static fr.enedis.teme.jquery.QueryParameterBuilder.parametrized;
import static fr.enedis.teme.jquery.SqlStringBuilder.COMA_SEPARATOR;
import static fr.enedis.teme.jquery.SqlStringBuilder.EMPTY_STRING;
import static fr.enedis.teme.jquery.SqlStringBuilder.POINT_SEPARATOR;
import static fr.enedis.teme.jquery.Validation.requireNonEmpty;
import static java.lang.System.currentTimeMillis;
import static java.lang.reflect.Array.getLength;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
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

	DBTable table;
	List<TaggableColumn> columns = new LinkedList<>();
	List<DBFilter> filters = new LinkedList<>();
	List<QueryResultJoiner> resultJoins = new LinkedList<>();
	
	public RequestQuery(RequestQuery query, DBTable table, boolean joins) {
		this.table = table;
		this.columns = new LinkedList<>(query.columns);
		this.filters = new LinkedList<>(query.filters);
		if(joins) {
			resultJoins = new LinkedList<>(query.resultJoins);
		}
	}
	
	public RequestQuery select(DBTable table, TaggableColumn... columns) {
		this.table = table;
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

	public RequestQuery innerJoinResult(boolean condition, Supplier<RequestQuery> supp) {
		return condition ? innerJoinResult(supp.get()) : this;
	}
	public RequestQuery innerJoinResult(RequestQuery query) {
		return resultJoin(INNER, query);
	}
	public RequestQuery leftJoinResult(boolean condition, Supplier<RequestQuery> supp) {
		return condition ? leftJoinResult(supp.get()) : this;
	}
	public RequestQuery leftJoinResult(RequestQuery query) {
		return resultJoin(LEFT, query);
	}
	public RequestQuery resultJoin(JoinType type, RequestQuery query) {
		this.resultJoins.add(new QueryResultJoiner(type, query));
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
		var sb = new SqlStringBuilder(estimateSize());
		String[] cols;
		if(resultJoins.isEmpty()) {
			build(schema, sb, pb);
			cols = getColumns().stream().map(TaggableColumn::tagname).toArray(String[]::new);
		}
		else {
			var map = new LinkedHashMap<String, String>();
			sb.append("SELECT ");
			columns("q0", sb, pb, map);
			var inc = new AtomicInteger();
			resultJoins.forEach(q-> q.columns("q"+inc.incrementAndGet(), sb.append(COMA_SEPARATOR), pb, map));
			sb.append(" FROM (");
			build(schema, sb, pb);
			sb.append(") q0");
			resultJoins.forEach(q-> q.build(schema, sb.append(EMPTY_STRING), pb));
			cols = map.keySet().toArray(String[]::new);
		}
		return new ParametredQuery(sb.toString(), cols, pb.getArgs().toArray());
	}

	@Override
	public void columns(String alias, SqlStringBuilder sb, QueryParameterBuilder pb, Map<String, String> columnMap) {//init map
		sb.appendEach(getColumns(), COMA_SEPARATOR, alias + POINT_SEPARATOR, c-> {
			columnMap.put(c.tagname(), alias);
			return c.tagname();
		});
	}
	
	@Override
	public void build(String schema, SqlStringBuilder sb, QueryParameterBuilder pb){
    	
    	requireNonNull(table);
    	requireNonEmpty(columns); 
    	sb.append("SELECT ")
    	.appendEach(columns, COMA_SEPARATOR, e-> e.tagSql(table, pb))
    	.append(" FROM ")
    	.append(table.sql(schema, pb));
    	if(!filters.isEmpty()) {
    		sb.append(" WHERE ")
    		.appendEach(filters, AND.sql(), f-> f.sql(table, pb));
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
	
	public RequestQuery fork(DBTable tab, boolean joins) {
		return new RequestQuery(tab, 
				new LinkedList<>(columns), 
				new LinkedList<>(filters),
				joins ? new LinkedList<>(resultJoins) : new LinkedList<>());
	}

	private static boolean groupable(DBColumn column) {
		return !column.isAggregation() && !column.isConstant();
	}
	
	int estimateSize() {
		return 500 * (resultJoins.size()+1);
	}
	
	public boolean isSimpleQuery() {
		return filters.isEmpty() 
				&& resultJoins.isEmpty() 
				&& columns.stream().noneMatch(DBColumn::isAggregation);
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

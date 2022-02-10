package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.DBColumn.columnJoiner;
import static fr.enedis.teme.jquery.DBColumn.joinColumns;
import static fr.enedis.teme.jquery.JoinType.INNER;
import static fr.enedis.teme.jquery.JoinType.LEFT;
import static fr.enedis.teme.jquery.LogicalOperator.AND;
import static fr.enedis.teme.jquery.QueryParameterBuilder.parametrized;
import static fr.enedis.teme.jquery.Validation.requireNonEmpty;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Getter(value = AccessLevel.PACKAGE)
public class RequestQuery {

	DBTable table;
	List<TaggableColumn> columns = new LinkedList<>();
	List<DBFilter> filters = new LinkedList<>();
	List<QueryResultJoiner> resultJoins = new LinkedList<>();
	
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

	public RequestQuery innerJoinResult(RequestQuery query, String... columns) {
		return resultJoin(INNER, query, columns);
	}
	public RequestQuery leftJoinResult(RequestQuery query, String... columns) {
		return resultJoin(LEFT, query, columns);
	}
	public RequestQuery resultJoin(JoinType type, RequestQuery query, String... columns) {
		this.resultJoins.add(new QueryResultJoiner(type, query, columns));
		return this;
	}
	
	public List<DynamicModel> execute(Function<ParametredQuery, List<DynamicModel>> fn) {
		return execute(null, fn);
	}

	public List<DynamicModel> execute(String schema, Function<ParametredQuery, List<DynamicModel>> fn) {

		requireNonNull(fn);
		var bg = currentTimeMillis();
		var query = build(schema);
		log.info("query built in {} ms", currentTimeMillis() - bg);
		bg = currentTimeMillis();
		var rows = fn.apply(query);
        log.info("query parameters : {}", Arrays.toString(query.getParams()));
		log.info("{} rows in {} ms", rows.size(), currentTimeMillis() - bg);
		return rows;
	}

	public final ParametredQuery build(String schema){
		
		var pb = parametrized();
		var sb = new StringBuilder(estimateSize());
		String[] cols;
		if(resultJoins.isEmpty()) {
			build(schema, sb, pb);
			cols = columns.stream().map(TaggableColumn::tagname).toArray(String[]::new);
		}
		else {
			var map = allColumns("q");
			sb.append("SELECT ")
			.append(map.entrySet().stream().map(e-> e.getValue()+"."+e.getKey()).collect(columnJoiner()))
			.append(" FROM (");
			build(schema, sb, pb);
			sb.append(") q0 ");
			resultJoins.forEach(q-> q.sql(schema, sb, pb, map));
			cols = map.keySet().toArray(String[]::new);
		}
		return new ParametredQuery(sb.toString(), cols, pb.getArgs().toArray());
	}
	
	protected void build(String schema, StringBuilder sb, QueryParameterBuilder pb){
    	
    	requireNonNull(table);
    	requireNonEmpty(columns); 
    	sb.append("SELECT ")
    	.append(columns.stream().map(e-> e.tagSql(table, pb)).collect(columnJoiner()))
    	.append(" FROM ")
    	.append(table.sql(schema, pb));
    	if(!filters.isEmpty()) {
    		sb.append(" WHERE ")
    		.append(filters.stream().map(f-> f.sql(table, pb)).collect(AND.joiner()));
    	}
        if(columns.stream().anyMatch(DBColumn::isAggregation)) {
        	var gc = columns.stream()
        			.filter(RequestQuery::groupable)
        			.map(TaggableColumn::tagname) //add alias 
        			.toArray(String[]::new);
        	if(gc.length > 0) {
        		sb.append(" GROUP BY ").append(joinColumns(gc));
        	}
        	else if(columns.size() > 1) {
        		//throw exception
        	}
        }
	}
	
	private Map<String, String> allColumns(String symbol) {
		var map = columnTags().stream().collect(toMap(identity(), t-> symbol+"0"));
		var cn = new AtomicInteger();
		resultJoins.forEach(q->{ //LinkedList iterator
			q.setAlias(symbol + cn.incrementAndGet());
			q.getRequest().getColumns().forEach(c-> map.putIfAbsent(c.tagname(), q.getAlias()));
		});
		return map;
	}
	
	List<String> columnTags(){
		return columns.stream()
				.map(TaggableColumn::tagname)
				.collect(toList());
	}

	public RequestQuery fork(DBTable tab) {
		return new RequestQuery(tab, 
				new LinkedList<>(columns), 
				new LinkedList<>(filters),
				new LinkedList<>(resultJoins));
	}

	private static boolean groupable(DBColumn column) {
		return !column.isAggregation() && !column.isConstant();
	}
	
	int estimateSize() {
		return 1000 * (resultJoins.size()+1);
	}
}

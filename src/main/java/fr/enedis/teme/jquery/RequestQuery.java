package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.JoinType.INNER;
import static fr.enedis.teme.jquery.JoinType.LEFT;
import static fr.enedis.teme.jquery.LogicalOperator.AND;
import static fr.enedis.teme.jquery.QueryParameterBuilder.parametrized;
import static fr.enedis.teme.jquery.Utils.concat;
import static fr.enedis.teme.jquery.Utils.isEmpty;
import static fr.enedis.teme.jquery.Validation.requireNonBlank;
import static fr.enedis.teme.jquery.Validation.requireNonEmpty;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.copyOf;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Getter(value = AccessLevel.PACKAGE)
public class RequestQuery {

	DBTable table;
	TaggableColumn[] columns;
	DBFilter[] filters;
	List<QueryDataJoiner> dataJoins = new LinkedList<>();
	List<QueryResultJoiner> resultJoins = new LinkedList<>();
	
	public RequestQuery select(DBTable table, TaggableColumn... columns) {
		this.table = table;
		return columns(columns);
	}

	public RequestQuery column(boolean condition, Supplier<TaggableColumn> column) {
		return condition ? columns(column.get()) : this;
	}
	public RequestQuery columns(boolean condition, Supplier<TaggableColumn[]> column) {
		return condition ? columns(column.get()) : this;
	}
	public RequestQuery columns(TaggableColumn... columns) {
		this.columns = concat(this.columns, columns);
		return this;
	}

	public RequestQuery filter(boolean condition, Supplier<DBFilter> filter){
		return condition ? filters(filter.get()) : this;
	}
	public RequestQuery filters(boolean condition, Supplier<DBFilter[]> filter){
		return condition ? filters(filter.get()) : this;
	}
	public RequestQuery filters(DBFilter... filters){
		this.filters = concat(this.filters, filters);
		return this;
	}

	public RequestQuery innerJoinData(RequestQuery query, JoinExpression ex) {
		return dataJoin(true, INNER, query, ex);
	}
	public RequestQuery innerJoinResult(RequestQuery query) {
		return dataJoin(false, INNER, query, null);
	}
	public RequestQuery leftJoinData(RequestQuery query, JoinExpression ex) {
		return dataJoin(true, LEFT, query, ex);
	}
	public RequestQuery leftJoinResult(RequestQuery query) {
		return dataJoin(false, LEFT, query, null);
	}
	public RequestQuery dataJoin(JoinType type, RequestQuery query, JoinExpression ex) {
		if(!query.getDataJoins().isEmpty()) {
			throw new UnsupportedOperationException("deep join"); //not 
		}
		this.dataJoins.add(new QueryDataJoiner(type, query,ex));
		return this;
	}
	
	public boolean isAggregation(){
		return Stream.of(columns).anyMatch(DBColumn::isAggregation);
	}
	
	public List<DynamicModel> execute(Function<ParametredQuery, List<DynamicModel>> fn) {
		return execute(null, fn);
	}

	public List<DynamicModel> execute(String schema, Function<ParametredQuery, List<DynamicModel>> fn) {

		requireNonNull(fn);
		var bg = currentTimeMillis();
		var query = build(schema);
		log.info("query built in {} ms", currentTimeMillis() - bg);
		var rows = fn.apply(query);
        log.info("query parameters : {}", Arrays.toString(query.getParams()));
		log.info("{} rows in {} ms", rows.size(), currentTimeMillis() - bg);
		return rows;
	}
	

	public final ParametredQuery build(String schema){
		
		var cb = new QueryColumnBuilder(schema, table, columns);
		var ph = parametrized();
		var qr = build(cb, ph);
		return new ParametredQuery(qr, cb.columns(), ph.getArgs().toArray());
	}
	
	public final String build(StringBuilder sb, QueryColumnBuilder cb, QueryParameterBuilder ph){

    	QueryDataJoiner[] dataJoins = isEmpty(joins)
    		? new QueryDataJoiner[] {} 
    		: Stream.of(joins)
    			.filter(QueryDataJoiner::isDataJoin)
    			.toArray(QueryDataJoiner[]::new);
		var param = build(null, cb, ph, dataJoins);
		if(joins != null && joins.length > dataJoins.length) {
			var sb = new StringBuilder();//size
			var jq = Stream.of(joins)
					.filter(not(QueryDataJoiner::isDataJoin))
					.collect(Collectors.toList());
			for(var q : jq) {
				q.appendColumns(cb);
			}
		}
		return param;
	}
	
	private void initialize(QueryColumnBuilder cb) {
		if(!dataJoins.isEmpty()) {
    		alias("t0");
    		for(var i=0; i<dataJoins.size(); i++) {
    			dataJoins.get(i).getRequest().alias("t"+(i+1));
    		}
		}
		for(var c : columns) {
			cb.appendColumn(c, table);
		}
		if(!dataJoins.isEmpty()) {
			for(var dj : dataJoins) {
				for(var c : dj.getRequest().getColumns()) {			
					cb.appendColumn(c, table); //no deep join
				}
			}
		}
	}

	protected void build(String suffix, StringBuilder sb, QueryColumnBuilder cb, QueryParameterBuilder ph){ //nullable
    	
    	requireNonNull(table);
    	requireNonEmpty(columns); 
    	boolean agg = isAggregation();
    	if(!dataJoins.isEmpty()) {
			agg |= dataJoins.stream().anyMatch(q-> q.getRequest().isAggregation()); 
    	}  
    	initialize(cb);
    	var entries= cb.entries();
    	sb.append("SELECT ").append(entries.stream().map(e-> e.getKey().sql(e.getValue(), ph)).collect(joining(", ")))
    		.append(" FROM ").append(table.sql(cb.getSchema(), suffix, ph));
    	if(!dataJoins.isEmpty()) {
    		dataJoins.stream().forEach(q-> q.sql(sb, cb, ph, null));
    	}
    	Stream<String> fs = filters(ph);
    	if(!dataJoins.isEmpty()) {
    		fs = Stream.concat(fs, dataJoins.stream().flatMap(q-> q.getRequest().filters(ph)));
    	}
     	var fList = fs.collect(AND.joiner());
    	if(!fList.isEmpty()) {
    		sb.append(" WHERE ").append(fList);
    	}
        if(agg) {
        	var gc = entries.stream()
        			.filter(e-> groupable(e.getKey()))
        			.map(e-> e.getValue().logicalColumnName(e.getKey())) //add alias 
        			.toArray(String[]::new);
        	if(gc.length > 0) {
        		sb.append(" GROUP BY ").append(String.join(",", gc));
        	}
        }
	}
	
	private Stream<String> filters(QueryParameterBuilder ph) {
		return filters == null 
				? Stream.empty()
				: Stream.of(filters).map(f-> f.sql(table, ph));
	}
	
	void alias(String alias) {
		this.table = new TableAlias(requireNonNull(table), requireNonBlank(alias));
	}

	public RequestQuery fork(DBTable tab) {
		return new RequestQuery(
				tab, 
				copyOf(columns, columns.length), 
				copyOf(filters, filters.length), 
				copyOf(dataJoins, dataJoins.length), 
				copyOf(resultJoins, resultJoins.length));
	}

	private static boolean groupable(DBColumn column) {
		return !column.isAggregation() && !column.isConstant();
	}
}

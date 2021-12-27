package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.ParameterHolder.parametrized;
import static fr.enedis.teme.jquery.Utils.concat;
import static fr.enedis.teme.jquery.Utils.isEmpty;
import static fr.enedis.teme.jquery.Validation.requireNonEmpty;
import static fr.enedis.teme.jquery.ValueColumn.staticColumn;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.sql.DataSource;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public final class QueryBuilder {
	
	private String schema;
	private DBTable table;
	private DBColumn[] columns;
	private DBFilter[] filters;
	private Function<DBTable, YearMonth[]> partitionFn;
			
	public QueryBuilder(RequestVariable rv) {
		columns(rv.getColumns());
		where(rv.getFilters());
		partition(rv.getPartitionFn());
	}
	
	public QueryBuilder select(String schema, DBTable table, DBColumn... columns) {
		this.schema = schema;
		this.table = table;
		return columns(columns);
	}
	
	public QueryBuilder select(DBTable table, DBColumn... columns) {
		this.table = table;
		return columns(columns);
	}

	public QueryBuilder columns(DBColumn... columns) {
		this.columns = concat(this.columns, columns);
		return this;
	}
	
	public QueryBuilder groupBy(DBColumn... columns) {
		return columns(columns);
	}

	public QueryBuilder groupBy(boolean condition, Supplier<DBColumn> column) {
		
		return condition ? columns(column.get()) : this;
	}

	public QueryBuilder where(DBFilter... filters){
		this.filters = concat(this.filters, filters);
		return this;
	}
	
	public QueryBuilder where(boolean condition, Supplier<DBFilter> filter){
		return condition ? where(filter.get()) : this;
	}
	
	public QueryBuilder partition(Function<DBTable, YearMonth[]> partitionFn){
		this.partitionFn = partitionFn;
		return this;
	}

	public List<DynamicModel> execute(DataSource ds){
		return execute(q-> q.execute(ds));
	}
	
	public List<DynamicModel> execute(Function<ParametredQuery, List<DynamicModel>> fn) {

		requireNonNull(fn);
		var bg = System.currentTimeMillis();
		var query = build();
		log.info("query built in {} ms", System.currentTimeMillis() - bg);
		var rows = fn.apply(query);
        log.info("query parameters : {}", Arrays.toString(query.getParams()));
		log.info("{} rows in {} ms", rows.size(), System.currentTimeMillis() - bg);
		return rows;
	}
	
	
	public ParametredQuery build(){
		
		if(partitionFn == null) {
			return build(schema, table, columns, filters, null);
		}
		var ymList = partitionFn.apply(table);
		if(isEmpty(ymList)) {
			return build(schema, table, columns, filters, null);
		}
		var map = Stream.of(ymList).collect(groupingBy(YearMonth::getYear));
		if(map.size() == 1) {//one table reference
			var e = map.entrySet().iterator().next();
			where(table.getRevisionColumn().inFilter(e.getValue().stream().map(YearMonth::getMonthValue).toArray(Integer[]::new)));
			if(e.getValue().size() > 1) {//add month rev. when multiple values
				columns(table.getRevisionColumn());
			}
			return build(schema, table, columns, filters, e.getKey());
		}
		var queries = map.entrySet().stream()
			.map(e-> {
				var ftrs = new DBFilter[]{table.getRevisionColumn().inFilter(e.getValue().stream().map(YearMonth::getMonthValue).toArray(Integer[]::new))}; //TD to int
				var cols = new DBColumn[]{table.getRevisionColumn(), staticColumn("revisionYear", e.getKey())}; //add year rev. when multiple values
				return build(schema, table, 
						concat(this.columns, cols), 
						concat(this.filters, ftrs), 
						e.getKey());
			})
			.collect(toList());
		return join(queries);
	}
	
	private static ParametredQuery build(String schema, DBTable table, DBColumn[] columns, DBFilter[] filters, Integer year){//year: nullable
    	
    	requireNonNull(table);
    	requireNonEmpty(columns);
    	
    	var ph = parametrized();

        var q = new StringBuilder(1000).append("SELECT ")
        		.append(Stream.of(columns)
            			.map(c-> c.sql(table, ph) + " AS " + c.tag(table))
            			.collect(joining(", ")))
        		.append(" FROM " + (year == null ? table.sql(schema, ph) : table.toSql(schema, year, ph)));
        if(!isEmpty(filters)) {
        	q = q.append(" WHERE ")
    			.append(Stream.of(filters)
	    			.map(f-> f.sql(table, ph))
	    			.collect(joining(" AND ")));
        }
        if(Stream.of(columns).anyMatch(DBColumn::isAggregation)) {
        	var gc = Stream.of(columns)
        			.filter(QueryBuilder::groupable)
        			.map(c-> c.tag(table))
        			.toArray(String[]::new);
        	if(gc.length > 0) {
        		q = q.append(" GROUP BY " + String.join(",", gc));
        	}
        }
        return new ParametredQuery(q.toString(), outputFormat(table, columns), ph.getArgs().toArray());
	}
	
	private static String[] outputFormat(DBTable table, DBColumn[] columns) {
		return Stream.of(columns)
			.map(c-> c.tag(table))
			.toArray(String[]::new);
	}
	
	//TD impl. collector
	private static final ParametredQuery join(Collection<ParametredQuery> queries) {
		requireNonEmpty(queries);
		//check columns ?
		return new ParametredQuery(
				queries.stream().map(ParametredQuery::getQuery).collect(joining(" UNION ")),
				queries.iterator().next().getColumnNames(),
				queries.stream().flatMap(o-> Stream.of(o.getParams())).toArray());
	}
	
	private static boolean groupable(DBColumn column) {
		return !column.isAggregation() && !column.isConstant();
	}
}

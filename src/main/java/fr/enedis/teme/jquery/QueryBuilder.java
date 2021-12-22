package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.ExpressionColumn.staticColumn;
import static fr.enedis.teme.jquery.Utils.concat;
import static fr.enedis.teme.jquery.Utils.isEmpty;
import static fr.enedis.teme.jquery.Validation.requireNonEmpty;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
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
	
	public QueryBuilder selectAll(String schema, DBTable table) {
		this.schema = schema;
		return select(table, table.getColumns());
	}
	
	public QueryBuilder selectAll(DBTable table) {
		return select(table, table.getColumns());
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
		
		return execute(q-> {
			List<DynamicModel> res;
			try(var cn = ds.getConnection()){
				try(var ps = cn.prepareStatement(q.getQuery())){
					if(q.getColumns() != null) {
						for(var i=0; i<q.getColumns().length; i++) {
							ps.setObject(i+1, q.getColumns()[i]);
						}						
					}
					try(var rs = ps.executeQuery()){
						res = new LinkedList<>();
						while(rs.next()) {
							res.add(q.map(rs));
						}
					}
				}
			}
			catch(Exception e) {
				throw new RuntimeException(e);
			}
			return res;
		});
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
			where(table.getRevisionColumn().in(e.getValue().stream().map(YearMonth::getMonthValue).toArray(Integer[]::new)));
			if(e.getValue().size() > 1) {//add month rev. when multiple values
				columns(table.getRevisionColumn());
			}
			return build(schema, table, columns, filters, e.getKey());
		}
		var queries = map.entrySet().stream()
			.map(e-> {
				var ftrs = new DBFilter[]{table.getRevisionColumn().in(e.getValue().stream().map(YearMonth::getMonthValue).toArray(Integer[]::new))}; //TD to int
				var cols = new DBColumn[]{table.getRevisionColumn(), staticColumn(e.getKey(), "revisionYear")}; //add year rev. when multiple values
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

        var q = new StringBuilder("SELECT ")
        		.append(joinColumns(table, columns))
        		.append(" FROM " + (year == null ? table.toSql(schema) : table.toSql(schema, year)));
        
        var filter = concat(table.getClauses(), filters);
        var args = new LinkedList<>();
        if(!isEmpty(filter)) {
        	q = q.append(" WHERE 1=1");
        	for(var f : filter) {
        		q = q.append(" AND ").append(f.toSql(table));
                args.addAll(f.args());
        	}
        }
        if(Stream.of(columns).anyMatch(DBColumn::isAggregated)) {
        	var gc = Stream.of(columns)
        			.filter(TableColumn.class::isInstance)
        			.map(c-> ((TableColumn)c).groupAlias(table))
        			.toArray(String[]::new);
        	if(gc.length > 0) {
        		q = q.append(" GROUP BY " + String.join(",", gc));
        	}
        }
        return new ParametredQuery(q.toString(), columns, args.toArray());
	}
	
	//TD impl. collector
	private static final ParametredQuery join(Collection<ParametredQuery> queries) {
		requireNonEmpty(queries);
		//check columns ?
		return new ParametredQuery(
				queries.stream().map(ParametredQuery::getQuery).collect(joining(" UNION ")), 
				queries.iterator().next().getColumns(), 
				queries.stream().flatMap(o-> Stream.of(o.getParams())).toArray());
	}
	
    private static final String joinColumns(DBTable table, DBColumn[] columns) {
    	
    	return Stream.of(columns)
    			.map(c-> c.toSql(table))
    			.collect(joining(", "));
    }
    
}

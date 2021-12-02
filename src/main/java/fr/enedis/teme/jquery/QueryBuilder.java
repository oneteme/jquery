package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.ExpressionColumn.staticColumn;
import static fr.enedis.teme.jquery.Utils.concat;
import static fr.enedis.teme.jquery.Utils.isEmpty;
import static fr.enedis.teme.jquery.Utils.requireNonEmpty;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.time.YearMonth;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class QueryBuilder {
	
	private DBTable table;
	private DBColumn[] columns;
	private DBFilter[] filters;

	public QueryBuilder selectAll(DBTable table) {
		return select(table, table.getColumns());
	}
	
	public QueryBuilder select(DBTable table, DBColumn... columns) {
		this.table = table;
		return columns(columns);
	}

	private QueryBuilder columns(DBColumn... columns) {
		this.columns = concat(this.columns, columns);
		return this;
	}
	
	public QueryBuilder groupBy(DBColumn... columns) {
		return columns(columns);
	}

	public QueryBuilder where(DBFilter... filters){
		this.filters = concat(this.filters, filters);
		return this;
	}
	
	public QueryBuilder having(DBFilter... filters){
		return where(filters);
	}

	public List<DynamicModel> execute(Function<ParametredQuery, List<DynamicModel>> fn) {
		
		return execute(null, fn);
	}
	
	public List<DynamicModel> execute(InFilter<YearMonth> partition, Function<ParametredQuery, List<DynamicModel>> fn) {
		
		requireNonNull(fn);
    	var bg = System.currentTimeMillis();
        var rows = fn.apply(build(partition));
        log.info("{} rows in {} ms", rows.size(), System.currentTimeMillis() - bg);
        return rows;
    }

	public List<DynamicModel> execute(DataSource ds){
		
		return execute(null, ds);
	}
	
	public List<DynamicModel> execute(InFilter<YearMonth> partition, DataSource ds){
		
		return execute(partition, q-> {
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
	
	public ParametredQuery build(InFilter<YearMonth> partition){

		if(partition == null || isEmpty(partition.getValues())) {
			return build(table, columns, filters, null);
		}
		var map = Stream.of(partition.getValues()).collect(groupingBy(YearMonth::getYear));
		if(map.size() == 1) {//one table reference
			var e = map.entrySet().iterator().next();
			where(partition.getColumn().in(e.getValue().stream().map(YearMonth::getMonthValue).toArray(Integer[]::new)).asVarChar()); //TD to int
			if(e.getValue().size() > 1) {//add month rev. when multiple values
				columns(partition.getColumn());
			}
			return build(table, columns, filters, e.getKey());
		}
		var queries = map.entrySet().stream()
			.map(e-> {
				var ftrs = new DBFilter[]{partition.getColumn().in(e.getValue().stream().map(YearMonth::getMonthValue).toArray(Integer[]::new)).asVarChar()}; //TD to int
				var cols = new DBColumn[]{partition.getColumn(), staticColumn(e.getKey(), "revisionYear")}; //add year rev. when multiple values
				return build(table, 
						concat(this.columns, cols), 
						concat(this.filters, ftrs), 
						e.getKey());
			})
			.collect(toList());
		return join(queries);
	}
	
	private static ParametredQuery build(DBTable table, DBColumn[] columns, DBFilter[] filters, Integer year){//year: nullable

    	var bg = System.currentTimeMillis();
    	
    	requireNonNull(table);
    	requireNonEmpty(columns, "columns");

        var q = new StringBuilder("SELECT ")
        		.append(joinColumns(table, columns))
        		.append(" FROM " + table.toSql(null, year)); //TD replace by getTableName(year)
        
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
        	var gc = Stream.of(columns).filter(TableColumn.class::isInstance).toArray(DBColumn[]::new);
        	if(gc.length == 0) {
        		throw new IllegalArgumentException("groupby expected");
        	}
        	q = q.append(" GROUP BY " + joinColumns(table, gc));
        }
        log.info("query built in {} ms", System.currentTimeMillis() - bg);
        return new ParametredQuery(q.toString(), columns, args.toArray());
	}
	
	//TD impl. collector
	private static final ParametredQuery join(Collection<ParametredQuery> queries) {
		if(requireNonNull(queries).isEmpty()) {
			throw new IllegalArgumentException("empty list"); //should never happen
		}
		//check columns ?
		String  query = queries.stream().map(ParametredQuery::getQuery).collect(joining(" UNION "));
		Object[] args = queries.stream().flatMap(o-> Stream.of(o.getParams())).toArray();
		return new ParametredQuery(query, queries.iterator().next().getColumns(), args);
	}
	
    private static final String joinColumns(DBTable table, DBColumn[] columns) {
    	
    	return Stream.of(columns)
    			.map(c-> c.toSql(table))
    			.collect(joining(", "));
    }
    
}

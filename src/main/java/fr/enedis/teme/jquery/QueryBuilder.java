package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Utils.concat;
import static fr.enedis.teme.jquery.Utils.isEmpty;
import static fr.enedis.teme.jquery.Utils.requireNonEmpty;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class QueryBuilder {
	
	private Table table;
	private Column[] columns;
	private Filter[] filters;

	public QueryBuilder selectAll(Table table) {
		return select(table, table.getColumns());
	}
	
	public QueryBuilder select(Table table, Column... columns) {
		this.table = table;
		this.columns = concat(this.columns, columns, Column[]::new);
		return this;
	}

	public QueryBuilder groupBy(Column... columns) {
		this.columns = concat(this.columns, columns, Column[]::new);
		return this;
	}

	public QueryBuilder where(Filter... filters){
		this.filters = concat(this.filters, filters, Filter[]::new);
		return this;
	}
	
	public QueryBuilder having(Filter... filters){
		return where(filters);
	}
	
	public ParametredQuery build(){

    	var bg = System.currentTimeMillis();
    	
    	requireNonNull(table);
    	requireNonEmpty(columns, "columns");

        var query = new StringBuilder("SELECT ")
        		.append(joinColumns(table, columns))
        		.append(" FROM " + table.getTableName());
        
        var filter = concat(table.getClauses(), filters, Filter[]::new);
        var argList = new LinkedList<>();
        if(!isEmpty(filter)) {
        	query = query.append(" WHERE 1=1");
        	for(var f : filter) {
        		query = query.append(" AND ").append(f.toSql(table));
                argList.addAll(f.args());
        	}
        }
        if(Stream.of(columns).anyMatch(Column::isAggregated)) {
        	var agColumns = Stream.of(columns).filter(groupByColumnsFilter()).toArray(Column[]::new);
        	if(agColumns.length == 0) {
        		throw new IllegalArgumentException("groupby expected");
        	}
        	query = query.append(" GROUP BY " + joinColumns(table, agColumns));
        }
        log.info("query built in {} ms", System.currentTimeMillis() - bg);
        return new ParametredQuery(query.toString(), columns, argList.toArray());
	}
	
	public List<DynamicModel> execute(Function<ParametredQuery, List<DynamicModel>> fn) {
		
		requireNonNull(fn);
    	var bg = System.currentTimeMillis();
        var rows = fn.apply(build());
        log.info("{} rows in {} ms", rows.size(), System.currentTimeMillis() - bg);
        return rows;
    }
	
    private static final String joinColumns(Table table, Column[] columns) {
    	
    	return Stream.of(columns)
    			.map(c-> c.toSql(table))
    			.collect(joining(", "));
    }
    
    private static final Predicate<Column> groupByColumnsFilter(){
    	
    	return c-> !c.isAggregated() && !c.isConstant();
    }

}

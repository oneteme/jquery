package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.ParameterHolder.parametrized;
import static fr.enedis.teme.jquery.Utils.concat;
import static fr.enedis.teme.jquery.Utils.isEmpty;
import static fr.enedis.teme.jquery.Validation.requireNonEmpty;
import static java.util.Arrays.copyOf;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class RequestQuery {

	DBTable table;
	DBColumn[] columns;
	DBFilter[] filters;
	String suffix; //internal fin
	
	public RequestQuery select(DBTable table, DBColumn... columns) {
		this.table = table;
		return columns(columns);
	}
	
	RequestQuery tableSuffix(String tableSuffix) {
		this.suffix = tableSuffix;
		return this;
	}

	public RequestQuery columns(DBColumn... columns) {
		this.columns = concat(this.columns, columns);
		return this;
	}
	
	public RequestQuery column(boolean condition, Supplier<DBColumn> column) {
		return condition ? columns(column.get()) : this;
	}

	public RequestQuery columns(boolean condition, Supplier<DBColumn[]> column) {
		return condition ? columns(column.get()) : this;
	}

	public RequestQuery filters(DBFilter... filters){
		this.filters = concat(this.filters, filters);
		return this;
	}
	
	public RequestQuery filter(boolean condition, Supplier<DBFilter> filter){
		return condition ? filters(filter.get()) : this;
	}

	public RequestQuery filters(boolean condition, Supplier<DBFilter[]> filter){
		return condition ? filters(filter.get()) : this;
	}

	public List<DynamicModel> execute(Function<ParametredQuery, List<DynamicModel>> fn) {
		return execute(null, fn);
	}

	public List<DynamicModel> execute(String schema, Function<ParametredQuery, List<DynamicModel>> fn) {

		requireNonNull(fn);
		var bg = System.currentTimeMillis();
		var query = build(schema);
		log.info("query built in {} ms", System.currentTimeMillis() - bg);
		var rows = fn.apply(query);
        log.info("query parameters : {}", Arrays.toString(query.getParams()));
		log.info("{} rows in {} ms", rows.size(), System.currentTimeMillis() - bg);
		return rows;
	}

	public ParametredQuery build(String schema){ //nullable
    	
    	requireNonNull(table);
    	requireNonEmpty(columns);
    	
    	var ph = parametrized();

        var q = new StringBuilder(1000).append("SELECT ")
        		.append(Stream.of(columns)
            			.map(c-> c.sql(table, ph) + " AS " + c.getTag())
            			.collect(joining(", ")))
        		.append(" FROM " + table.sql(schema, suffix, ph));
        if(!isEmpty(filters)) {
        	q = q.append(" WHERE ")
    			.append(Stream.of(filters)
	    			.map(f-> f.sql(table, ph))
	    			.collect(joining(" AND ")));
        }
        if(Stream.of(columns).anyMatch(DBColumn::isAggregation)) {
        	var gc = Stream.of(columns)
        			.filter(RequestQuery::groupable)
        			.map(DBColumn::getTag)
        			.toArray(String[]::new);
        	if(gc.length > 0) {
        		q = q.append(" GROUP BY " + String.join(",", gc));
        	}
        }
        return new ParametredQuery(
        		q.toString(), 
        		Stream.of(columns).map(DBColumn::getTag).toArray(String[]::new), 
        		ph.getArgs().toArray());
	}
	
	public RequestQuery fork(DBTable tab) {
		return new RequestQuery(
				tab, 
				copyOf(columns, columns.length), 
				copyOf(filters, filters.length), suffix);
	}
	
	private static boolean groupable(DBColumn column) {
		return !column.isAggregation() && !column.isConstant();
	}
}

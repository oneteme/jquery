package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.LogicalOperator.AND;
import static fr.enedis.teme.jquery.ParameterHolder.parametrized;
import static fr.enedis.teme.jquery.Utils.concat;
import static fr.enedis.teme.jquery.Utils.isEmpty;
import static fr.enedis.teme.jquery.Validation.requireNonEmpty;
import static java.util.Arrays.copyOf;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class RequestQuery {

	DBTable table;
	TaggableColumn[] columns;
	DBFilter[] filters;
	String suffix; //internal fin
	
	public RequestQuery select(DBTable table, TaggableColumn... columns) {
		this.table = table;
		return columns(columns);
	}
	
	RequestQuery tableSuffix(String tableSuffix) {
		this.suffix = tableSuffix;
		return this;
	}

	public RequestQuery columns(TaggableColumn... columns) {
		this.columns = concat(this.columns, columns);
		return this;
	}
	
	public RequestQuery column(boolean condition, Supplier<TaggableColumn> column) {
		return condition ? columns(column.get()) : this;
	}

	public RequestQuery columns(boolean condition, Supplier<TaggableColumn[]> column) {
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
		return execute(null, null, fn);
	}

	public List<DynamicModel> execute(RequestQuery req, Function<ParametredQuery, List<DynamicModel>> fn) {
		return execute(null, req, fn);
	}

	public List<DynamicModel> execute(String schema, RequestQuery req, Function<ParametredQuery, List<DynamicModel>> fn) {

		requireNonNull(fn);
		var bg = System.currentTimeMillis();
		var query = req == null ? build(schema) : join(schema, req);
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
            			.map(c-> c.sql(table, ph) + " AS " + c.tagname()) //important tag
            			.collect(joining(", ")))
        		.append(" FROM " + table.sql(schema, suffix, ph));
        if(!isEmpty(filters)) {
        	q = q.append(" WHERE ")
    			.append(Stream.of(filters)
	    			.map(f-> f.sql(table, ph))
	    			.collect(joining(AND.toString())));
        }
        if(Stream.of(columns).anyMatch(DBColumn::isAggregation)) {
        	var gc = Stream.of(columns)
        			.filter(RequestQuery::groupable)
        			.map(TaggableColumn::tagname)
        			.toArray(String[]::new);
        	if(gc.length > 0) {
        		q = q.append(" GROUP BY " + String.join(",", gc));
        	}
        }
        return new ParametredQuery(
        		q.toString(), 
        		Stream.of(columns).map(TaggableColumn::tagname).toArray(String[]::new), 
        		ph.getArgs().toArray());
	}
	
	public RequestQuery fork(DBTable tab) {
		return new RequestQuery(
				tab, 
				copyOf(columns, columns.length), 
				copyOf(filters, filters.length), suffix);
	}
	
	public ParametredQuery join(String schema, @NonNull RequestQuery req) { //
		
		var leftCols = Stream.of(columns).map(TaggableColumn::tagname).collect(toList());
		var add = new LinkedList<String>();
		var com = new LinkedList<String>();
		for(var c : req.columns) {
			var tag = c.tagname();
			(leftCols.contains(tag) ? com : add).add(tag);
		}
		var r1 = this.build(schema);
		var r2 = req.build(schema);
		StringBuilder sb = new StringBuilder(1000)
				.append("SELECT ")
				.append(concat(leftCols.stream().map("t0."::concat), add.stream().map("t1."::concat)).collect(joining(", ")))
				.append(" FROM (").append(r1.getQuery()).append(") t0")
				.append(" LEFT JOIN (").append(r2.getQuery()).append(") t1")
				.append(" ON ").append(com.stream().map(c-> "t1." + c + "=" + "t0." + c).collect(joining(AND.toString())));
		return new ParametredQuery(
				sb.toString(), 
				Stream.concat(leftCols.stream(), add.stream()).toArray(String[]::new),
				Utils.concat(r1.getParams(), r2.getParams(), Object[]::new));
	}
	
	private static boolean groupable(DBColumn column) {
		return !column.isAggregation() && !column.isConstant();
	}
	
}

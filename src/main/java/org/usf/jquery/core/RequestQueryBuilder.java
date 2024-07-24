package org.usf.jquery.core;

import static java.lang.System.currentTimeMillis;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.usf.jquery.core.Database.TERADATA;
import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.QueryParameterBuilder.parametrized;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Utils.currentDatabase;
import static org.usf.jquery.core.Validation.requireNonEmpty;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@Getter
public class RequestQueryBuilder {

	private final List<TaggableColumn> columns = new LinkedList<>();
	private final List<DBFilter> filters = new LinkedList<>();  //WHERE & HAVING
	private final List<DBView> views = new LinkedList<>();
	private final List<DBOrder> orders = new LinkedList<>();
	private final List<ViewJoin> joins = new LinkedList<>(); 
	private Iterator<?> it;
	private boolean distinct;
	private Integer fetch;
	private Integer offset;
	
	public RequestQueryBuilder distinct() {
		distinct = true;
		return this;
	}
	
	public RequestQueryBuilder views(@NonNull DBView... views) {
		Stream.of(views).forEach(this.views::add);
		return this;
	}

	public RequestQueryBuilder columns(@NonNull TaggableColumn... columns) {
		Stream.of(columns).forEach(this.columns::add);
		return this;
	}

	public RequestQueryBuilder filters(@NonNull DBFilter... filters){
		Stream.of(filters).forEach(this.filters::add);
		return this;
	}
	
	public RequestQueryBuilder orders(@NonNull DBOrder... orders) {
		Stream.of(orders).forEach(this.orders::add);
		return this;
	}

	public RequestQueryBuilder joins(@NonNull ViewJoin joins) {
		Stream.of(joins).forEach(this.joins::add);
		return this;
	}

	// the LIMIT clause is not in SQL standard.
	public RequestQueryBuilder fetch(Integer offset, Integer fetch) {
		return offset(offset).fetch(fetch);
	}
	
	public RequestQueryBuilder fetch(Integer fetch) {
		this.fetch = fetch;
		return this;
	}
	
	public RequestQueryBuilder offset(Integer offset) {
		this.offset = offset;
		return this;
	}
	
	public RequestQueryBuilder repeat(@NonNull Iterator<?> it) {
		this.it = it;
		return this;
	}
	
	public Optional<TaggableColumn> getColumn(String id){
		return columns.stream().filter(c-> c.tagname().contains(id)).findAny();
	}
	
	public QueryView as(String tag) {
		return new QueryView(tag, this);
	}

	public RequestQuery build(){
		return build(null);
	}

	public RequestQuery build(String schema) {
		log.trace("building query...");
//		requireNonEmpty(tables);
    	requireNonEmpty(columns);
		var bg = currentTimeMillis();
		var pb = parametrized(schema, views);
		var sb = new SqlStringBuilder(1000); //avg
		if(isNull(it)) {
			build(sb, pb);
		}
		else {
			sb.forEach(it, " UNION ALL ", o-> build(sb, pb));
		}
		log.trace("query built in {} ms", currentTimeMillis() - bg);
		return new RequestQuery(sb.toString(), pb.args(), pb.argTypes());
	}

	public final void build(SqlStringBuilder sb, QueryParameterBuilder pb){
		views.forEach(pb::view);
		select(sb, pb);
		var queryIdx = sb.sb.length(); //pos mark
		var argsIdx = pb.argCount();
    	where(sb, pb);
    	groupBy(sb, pb);
    	having(sb, pb);
    	orderBy(sb, pb);
    	fetch(sb);
		sb.setOffset(queryIdx);
		pb.setIndex(argsIdx);
    	from(sb, pb); //declare all view before FROM)
    	join(sb, pb);
	}

	void select(SqlStringBuilder sb, QueryParameterBuilder pb){
		if(currentDatabase() == TERADATA) {
			if(nonNull(offset)) {
				throw new UnsupportedOperationException("");
			}
			if(distinct && nonNull(fetch)) {
				throw new UnsupportedOperationException("Top N option is not supported with DISTINCT option.");
			}
		}
		sb.append("SELECT")
    	.appendIf(distinct, " DISTINCT")
    	.appendIf(nonNull(fetch) && currentDatabase() == TERADATA, ()-> " TOP " + fetch) //???????
    	.append(SPACE)
    	.appendEach(columns, SCOMA, o-> o.sqlWithTag(pb));
	}
	
	void from(SqlStringBuilder sb, QueryParameterBuilder pb) {
		var vList = pb.views();
		if(!joins.isEmpty()) {
			vList = vList.stream()
					.filter(v-> joins.stream().noneMatch(j-> j.id().equals(v.id())))
					.collect(toList());
		}
		if(!vList.isEmpty()) {
			sb.append(" FROM ")
				.appendEach(vList, SCOMA, o-> o.sqlWithTag(pb));
		}
	}
	
	void join(SqlStringBuilder sb, QueryParameterBuilder pb) {
		if(!joins.isEmpty()) {
			sb.append(SPACE).appendEach(joins, SPACE, v-> v.sql(pb));
		}
	}

	void where(SqlStringBuilder sb, QueryParameterBuilder pb){
		var expr = filters.stream()
				.filter(not(DBFilter::isAggregation))
				.map(f-> f.sql(pb))
    			.collect(joining(AND.sql()));
    	if(!expr.isEmpty()) {
    		sb.append(" WHERE ").append(expr);
    	}
	}
	
	void groupBy(SqlStringBuilder sb, QueryParameterBuilder pb){
        if(isAggregation()) { // also check filter
        	var expr = columns.stream()
        			.filter(not(DBColumn::isAggregation))
        			.flatMap(DBColumn::groupKeys)
        			.map(c-> columns.contains(c) ? ((TaggableColumn)c).tagname() : c.sql(pb)) //add alias 
        			.collect(joining(SCOMA));
        	if(!expr.isEmpty()) {
        		sb.append(" GROUP BY ").append(expr);
        	}
        }
	}
	
	void having(SqlStringBuilder sb, QueryParameterBuilder pb){
		var having = filters.stream()
				.filter(DBFilter::isAggregation)
				.collect(toList());
    	if(!having.isEmpty()) {
    		sb.append(" HAVING ")
    		.appendEach(having, AND.sql(), f-> f.sql(pb));
    	}
	}
	
	void orderBy(SqlStringBuilder sb, QueryParameterBuilder pb) {
    	if(!orders.isEmpty()) {
    		sb.append(" ORDER BY ")
    		.appendEach(orders, SCOMA, o-> o.sql(pb));
    	}
	}
	
	void fetch(SqlStringBuilder sb) {
		if(currentDatabase() != TERADATA) { // TOP n
			if(nonNull(offset)) {
				sb.append(" OFFSET ").append(offset.toString()).append(" ROWS");
			}
			if(nonNull(fetch)) {
				sb.append(" FETCH NEXT ").append(fetch.toString()).append(" ROWS ONLY");
			}
		}
	}
	
	public boolean isAggregation() {
		return columns.stream().anyMatch(Aggregable::isAggregation) ||
				filters.stream().anyMatch(Aggregable::isAggregation);
	}

	@Override
	public String toString() {
		return this.build().getQuery();
	}
}

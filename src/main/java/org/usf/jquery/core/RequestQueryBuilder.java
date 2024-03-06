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
	private final List<DBFilter> filters = new LinkedList<>();  //WERE & HAVING
	private final List<DBView> views = new LinkedList<>();
	private final List<DBOrder> orders = new LinkedList<>();
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
	
	public ViewQuery as(String tag) {
		return new ViewQuery(tag, this);
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
		var queryIdx = sb.sb.length();
		var argsIdx = pb.argCount();
    	where(sb, pb);
    	groupBy(sb, pb);
    	having(sb, pb);
    	orderBy(sb, pb);
    	fetch(sb);
    	from(sb, pb, queryIdx, argsIdx); //declare all view before FROM)
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
    	.appendIf(nonNull(fetch), ()-> " TOP " + fetch) //???????
    	.append(SPACE)
    	.appendEach(columns, SCOMA, o-> o.sqlWithTag(pb));
	}
	
	void from(SqlStringBuilder sb, QueryParameterBuilder pb, int queryIdx, int argsIdx) {
		if(!pb.views().isEmpty()) {
			sb.setOffset(queryIdx);
			pb.setIndex(argsIdx);
			sb.append(" FROM ")
				.appendEach(pb.views(), SCOMA, o-> o.sqlWithTag(pb));
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
        if(isAggregation()) { // check filter
        	var expr = columns.stream()
        			.filter(not(DBColumn::isAggregation))
        			.flatMap(DBColumn::groupKeys)
        			.map(c-> columns.contains(c) ? ((TaggableColumn)c).tagname() : c.sql(pb)) //add alias 
        			.collect(joining(SCOMA));
        	if(!expr.isEmpty()) {
        		sb.append(" GROUP BY ").append(expr);
        	}
        	else if(columns.size() > 1) {
        		//throw new RuntimeException("require groupBy columns"); CONST !?
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
		return columns.stream().anyMatch(DBColumn::isAggregation) ||
				filters.stream().anyMatch(DBFilter::isAggregation);
	}

	@Override
	public String toString() {
		return this.build().getQuery();
	}
}

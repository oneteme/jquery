package org.usf.jquery.core;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.addAll;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.Database.TERADATA;
import static org.usf.jquery.core.Database.currentDatabase;
import static org.usf.jquery.core.Database.setCurrentDatabase;
import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.QueryParameterBuilder.parametrized;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Validation.requireNonEmpty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

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
public class RequestQueryBuilder implements RequestContext {
	
	private final List<TaggableColumn> columns = new ArrayList<>();
	private final List<DBFilter> filters = new ArrayList<>();  //WHERE & HAVING
	private final List<DBOrder> orders = new ArrayList<>();
	private final List<ViewJoin> joins = new ArrayList<>(); 
	private final Map<DBView, QueryView> overView = new HashMap<>();
	private Iterator<?> it;
	private boolean distinct;
	private Integer fetch;
	private Integer offset;
	
	public RequestQueryBuilder() {
		this(null);
	}
	
	public RequestQueryBuilder(Database target) {
		setCurrentDatabase(target);
	}
	
	@Override
	public Optional<TaggableColumn> declaredColumn(String name) {
		return columns.stream().filter(c-> name.equals(c.tagname())).findAny();
	}
	
	@Override
	public QueryView overView(DBView view, Supplier<QueryView> supp) {
		return overView.computeIfAbsent(view, k-> supp.get());
	}
	
	public RequestQueryBuilder columns(@NonNull TaggableColumn... columns) {
		addAll(this.columns, columns);
		return this;
	}

	public RequestQueryBuilder filters(@NonNull DBFilter... filters){
		addAll(this.filters, filters);
		return this;
	}
	
	public RequestQueryBuilder orders(@NonNull DBOrder... orders) {
		addAll(this.orders, orders);
		return this;
	}

	public RequestQueryBuilder joins(@NonNull ViewJoin... joins) {
		addAll(this.joins, joins);
		return this;
	}
	
	public RequestQueryBuilder repeat(@NonNull Iterator<?> it) {
		this.it = it;
		return this;
	}
	
	public RequestQueryBuilder fetch(int fetch) {
		this.fetch = fetch;
		return this;
	}
	
	public RequestQueryBuilder offset(int offset) {
		this.offset = offset;
		return this;
	}
	
	public RequestQueryBuilder distinct() {
		distinct = true;
		return this;
	}
	
	@Deprecated
	public RequestQueryBuilder overViews(Map<DBView, QueryView> overs) {
		overView.putAll(overs);
		return this;
	}
	
	public QueryView asView() {
		return new QueryView(this);
	}

	public RequestQuery build(){
		return build(null);
	}

	public RequestQuery build(String schema) {
		log.trace("building query...");
    	requireNonEmpty(columns, "columns");
		var bg = currentTimeMillis();
		var pb = parametrized(schema, overView); //over clause
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
		var sub = new SqlStringBuilder(100);
		join(sub, pb);
    	where(sub, pb);
    	groupBy(sub, pb);
    	having(sub, pb);
    	orderBy(sub, pb);
    	fetch(sub);
    	select(sb, pb);
		from(sb, pb); //enumerate all view before from clause
		sb.append(sub.toString()); //TODO optim
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
		var excludes = joins.stream().map(ViewJoin::getView).toList();
		var views = pb.views().stream().filter(not(excludes::contains)).toList(); //do not remove views
		if(!views.isEmpty()) {
			sb.append(" FROM ").appendEach(views, SCOMA, v-> v.sqlWithTag(pb));
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
        			.map(c-> !(c instanceof ViewColumn) && columns.contains(c) ? ((TaggableColumn)c).tagname() : c.sql(pb)) //add alias 
        			.collect(joining(SCOMA));
        	if(!expr.isEmpty()) {
        		sb.append(" GROUP BY ").append(expr);
        	}
        }
	}
	
	void having(SqlStringBuilder sb, QueryParameterBuilder pb){
		var having = filters.stream()
				.filter(DBFilter::isAggregation)
				.toList();
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
		return columns.stream().anyMatch(Nested::isAggregation) ||
				filters.stream().anyMatch(Nested::isAggregation);
	}

	@Override
	public String toString() {
		return this.build().getQuery();
	}
}

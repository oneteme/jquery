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
import static org.usf.jquery.core.QueryVariables.parameterized;
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
public class QueryBuilder implements QueryContext {
	
	private final List<NamedColumn> columns = new ArrayList<>();
	private final List<DBFilter> filters = new ArrayList<>();  //WHERE & HAVING
	private final List<DBOrder> orders = new ArrayList<>();
	private final List<ViewJoin> joins = new ArrayList<>(); 
	private final Map<DBView, QueryView> overView = new HashMap<>();
	private boolean distinct;
	private Integer fetch;
	private Integer offset;
	private Iterator<?> it;
	
	public QueryBuilder() {
		this(null);
	}
	
	public QueryBuilder(Database target) {
		setCurrentDatabase(target);
	}
	
	@Override
	public Optional<NamedColumn> lookupDeclaredColumn(String name) {
		return columns.stream()
				.filter(ColumnProxy.class::isInstance)
				.filter(c-> name.equals(c.getTag()))
				.findAny();
	}
	
	@Override
	public QueryView overView(DBView view, Supplier<QueryView> supp) {
		return overView.computeIfAbsent(view, k-> supp.get());
	}
	
	public QueryBuilder columns(@NonNull NamedColumn... columns) {
		addAll(this.columns, columns); //add only if !exits
		return this;
	}

	public QueryBuilder filters(@NonNull DBFilter... filters){
		addAll(this.filters, filters);
		return this;
	}
	
	public QueryBuilder orders(@NonNull DBOrder... orders) {
		addAll(this.orders, orders);
		return this;
	}

	public QueryBuilder joins(@NonNull ViewJoin... joins) {
		addAll(this.joins, joins);
		return this;
	}
	
	public QueryBuilder fetch(int fetch) {
		this.fetch = fetch;
		return this;
	}
	
	public QueryBuilder offset(int offset) {
		this.offset = offset;
		return this;
	}
	
	public QueryBuilder distinct() {
		distinct = true;
		return this;
	}
	
	public QueryBuilder repeat(@NonNull Iterator<?> it) {
		this.it = it;
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
		var pb = parameterized(schema, overView); //over clause
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

	public final void build(SqlStringBuilder sb, QueryVariables pb){
		var sub = new SqlStringBuilder(100);
		join(sub, pb);
    	where(sub, pb);
    	groupBy(sub, pb);
    	having(sub, pb);
    	orderBy(sub, pb);
    	fetch(sub);
    	select(sb, pb);
		from(sb, pb); //enumerate all views before from clause
		sb.append(sub.toString()); //TODO optim
	}

	void select(SqlStringBuilder sb, QueryVariables pb){
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
	
	void from(SqlStringBuilder sb, QueryVariables pb) {
		var excludes = joins.stream().map(ViewJoin::getView).map(pb::viewOverload).toList();
		var views = pb.views().stream().filter(not(excludes::contains)).toList(); //do not remove views
		if(!views.isEmpty()) {
			sb.append(" FROM ").appendEach(views, SCOMA, v-> v.sqlWithTag(pb));
		}
	}
	
	void join(SqlStringBuilder sb, QueryVariables pb) {
		if(!joins.isEmpty()) {
			sb.append(SPACE).appendEach(joins, SPACE, v-> v.sql(pb));
		}
	}

	void where(SqlStringBuilder sb, QueryVariables pb){
		var expr = filters.stream()
				.filter(not(DBFilter::isAggregation))
				.map(f-> f.sql(pb))
    			.collect(joining(AND.sql()));
    	if(!expr.isEmpty()) {
    		sb.append(" WHERE ").append(expr);
    	}
	}
	
	void groupBy(SqlStringBuilder sb, QueryVariables pb){
        if(isAggregation()) { // also check filter
        	var expr = columns.stream()
        			.filter(not(DBColumn::isAggregation))
        			.flatMap(DBColumn::groupKeys)
        			.map(c-> !(c instanceof ViewColumn) && columns.contains(c) ? ((NamedColumn)c).getTag() : c.sql(pb)) //add alias 
        			.collect(joining(SCOMA));
        	if(!expr.isEmpty()) {
        		sb.append(" GROUP BY ").append(expr);
        	}
        }
	}
	
	void having(SqlStringBuilder sb, QueryVariables pb){
		var having = filters.stream()
				.filter(DBFilter::isAggregation)
				.toList();
    	if(!having.isEmpty()) {
    		sb.append(" HAVING ")
    		.appendEach(having, AND.sql(), f-> f.sql(pb));
    	}
	}
	
	void orderBy(SqlStringBuilder sb, QueryVariables pb) {
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

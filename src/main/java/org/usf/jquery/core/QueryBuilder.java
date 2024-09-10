package org.usf.jquery.core;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.addAll;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.Clause.COLUMN;
import static org.usf.jquery.core.Clause.FILTER;
import static org.usf.jquery.core.Clause.ORDER;
import static org.usf.jquery.core.DBColumn.allColumns;
import static org.usf.jquery.core.Database.TERADATA;
import static org.usf.jquery.core.Database.currentDatabase;
import static org.usf.jquery.core.Database.setCurrentDatabase;
import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.QueryContext.parameterized;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Validation.requireNonEmpty;
import static org.usf.jquery.web.ResourceAccessException.resourceAlreadyExistsException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
public class QueryBuilder {
	
	private final List<NamedColumn> columns = new ArrayList<>();
	private final List<DBColumn> group = new ArrayList<>(); 
	private final List<DBFilter> where = new ArrayList<>(); 
	private final List<DBFilter> having = new ArrayList<>();
	private final List<DBOrder> orders = new ArrayList<>();
	private final List<ViewJoin> joins = new ArrayList<>(); 
	private final Map<DBView, QueryView> overView = new HashMap<>();
	private boolean distinct;
	private boolean aggregation;
	private Integer fetch;
	private Integer offset;
	private Iterator<?> it;
	private Clause clause;
	
	public QueryBuilder() {
		this(null);
	}
	
	public QueryBuilder(Database target) {
		setCurrentDatabase(target);
	}

	public QueryView overView(DBView view) {
		return overView(view, ()-> new QueryBuilder().columns(allColumns(view)).asView());
	}
	
	public QueryView overView(DBView view, Supplier<QueryView> supp) {
		return overView.computeIfAbsent(view, k-> supp.get());
	}
	
	public QueryBuilder columns(@NonNull NamedColumn... columns) {
		this.clause = COLUMN;
		for(var col : columns) { //optional tag
			if(nonNull(col.getTag()) && this.columns.stream()
					.filter(c-> nonNull(c.getTag()))
					.anyMatch(nc-> nc.getTag().equals(col.getTag()))) {
				throw resourceAlreadyExistsException(col.getTag());
			}
			this.columns.add(col);
			if(!col.resolve(this)) {
				group.add(col);
			}
		}
		return this;
	}

	public QueryBuilder filters(@NonNull DBFilter... filters){
		this.clause = FILTER;
		for(var f : filters) {
			(f.resolve(this) ? having : where).add(f);
		}
		return this;
	}
	
	public QueryBuilder orders(@NonNull DBOrder... orders) {
		this.clause = ORDER;
		for(var o : orders) {
			this.orders.add(o);
			var c = o.getColumn();
			if(!c.resolve(this)) {
				this.group.add(c);
			}
		}
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
	
	QueryBuilder aggregation() {
		this.aggregation = true;
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

	public final void build(SqlStringBuilder sb, QueryContext ctx){
		var sub = new SqlStringBuilder(100);
    	where(sub, ctx); //first resolve over view
    	groupBy(sub, ctx);
    	having(sub, ctx);
    	orderBy(sub, ctx);
    	fetch(sub, ctx);
    	select(sb, ctx);
		from(sb, ctx); //enumerate all views before from clause
		join(sb, ctx);
		sb.append(sub.toString()); //TODO optim
	}

	void select(SqlStringBuilder sb, QueryContext ctx){
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
    	.appendEach(columns, SCOMA, o-> o.sqlWithTag(ctx));
	}
	
	void from(SqlStringBuilder sb, QueryContext ctx) {
		var excludes = joins.stream().map(ViewJoin::getView).toList();
		var views = ctx.views().stream().filter(not(excludes::contains)).toList(); //do not remove views
		if(!views.isEmpty()) {
			sb.append(" FROM ").appendEach(views, SCOMA, v-> v.sqlWithTag(ctx));
		}
	}
	
	void join(SqlStringBuilder sb, QueryContext ctx) {
		if(!joins.isEmpty()) {
			sb.append(SPACE).appendEach(joins, SPACE, v-> v.sql(ctx));
		}
	}

	void where(SqlStringBuilder sb, QueryContext ctx){
		if(!where.isEmpty()) {
    		sb.append(" WHERE ").appendEach(where, AND.sql(), f-> f.sql(ctx));
		}
	}
	
	void groupBy(SqlStringBuilder sb, QueryContext ctx){
		if(aggregation && !group.isEmpty()) {
    		sb.append(" GROUP BY ")
    		.append(group.stream()
    				.map(c-> !(c instanceof ViewColumn) && columns.contains(c) ? ((NamedColumn)c).getTag() : c.sql(ctx)) //add alias 
        			.collect(joining(SCOMA)));
		}
	}
	
	void having(SqlStringBuilder sb, QueryContext ctx){
		if(!having.isEmpty()) {
    		sb.append(" HAVING ")
    		.appendEach(having, AND.sql(), f-> f.sql(ctx));
		}
	}
	
	void orderBy(SqlStringBuilder sb, QueryContext ctx) {
    	if(!orders.isEmpty()) {
    		sb.append(" ORDER BY ")
    		.appendEach(orders, SCOMA, o-> o.sql(ctx));
    	}
	}
	
	void fetch(SqlStringBuilder sb, QueryContext ctx) {
		if(currentDatabase() != TERADATA) { // TOP n
			if(nonNull(offset)) {
				sb.append(" OFFSET ").append(offset.toString()).append(" ROWS");
			}
			if(nonNull(fetch)) {
				sb.append(" FETCH NEXT ").append(fetch.toString()).append(" ROWS ONLY");
			}
		}
	}

	@Override
	public String toString() {
		return this.build().getQuery();
	}
}

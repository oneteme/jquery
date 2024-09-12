package org.usf.jquery.core;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.addAll;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.function.Predicate.not;
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
	private Integer limit;
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
			if(this.orders.add(o) && !o.resolve(this)) {
				this.group.add(o.getColumn());
			}
		}
		return this;
	}

	public QueryBuilder joins(@NonNull ViewJoin... joins) {
		addAll(this.joins, joins);
		return this;
	}
	
	public QueryBuilder limit(Integer limit) {
		this.limit = limit;
		return this;
	}
	
	public QueryBuilder offset(Integer offset) {
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
    	fetch(sub);
    	select(sb, ctx);
		from(sb, ctx); //enumerate all views before from clause
		join(sb, ctx);
		sb.append(sub.toString());
	}

	void select(SqlStringBuilder sb, QueryContext ctx){
		if(currentDatabase() == TERADATA) {
			if(nonNull(offset)) {
				throw new UnsupportedOperationException("");
			}
			if(distinct && nonNull(limit)) {
				throw new UnsupportedOperationException("Top N option is not supported with DISTINCT option.");
			}
		}
		sb.append("SELECT")
    	.appendIf(distinct, " DISTINCT")
    	.appendIf(nonNull(limit) && currentDatabase() == TERADATA, ()-> " TOP " + limit) //???????
    	.space()
    	.forEach(columns.iterator(), SCOMA, o-> o.sqlWithTag(sb, ctx));
	}
	
	void from(SqlStringBuilder sb, QueryContext ctx) {
		var excludes = joins.stream().map(ViewJoin::getView).toList();
		var views = ctx.views().stream().filter(not(excludes::contains)).toList(); //do not remove views
		if(!views.isEmpty()) {
			sb.from().forEach(views.iterator(), SCOMA, v-> v.sqlWithTag(sb, ctx));
		}
	}
	
	void join(SqlStringBuilder sb, QueryContext ctx) {
		if(!joins.isEmpty()) {
			sb.space().forEach(joins.iterator(), SPACE, v-> v.sql(sb, ctx));
		}
	}

	void where(SqlStringBuilder sb, QueryContext ctx){
		if(!where.isEmpty()) {
    		sb.append(" WHERE ").forEach(where.iterator(), AND.sql(), f-> f.sql(sb, ctx));
		}
	}
	
	void groupBy(SqlStringBuilder sb, QueryContext ctx){
		if(aggregation && !group.isEmpty()) {
    		sb.append(" GROUP BY ").forEach(group.iterator(), SCOMA, c-> {
    			if(!(c instanceof ViewColumn) && columns.contains(c)) {
    				sb.append(((NamedColumn)c).getTag());
    			}
    			else {
    				 c.sql(sb, ctx);
    			}
    		});
		}
	}
	
	void having(SqlStringBuilder sb, QueryContext ctx){
		if(!having.isEmpty()) {
    		sb.append(" HAVING ")
    		.forEach(having.iterator(), AND.sql(), f-> f.sql(sb, ctx));
		}
	}
	
	void orderBy(SqlStringBuilder sb, QueryContext ctx) {
    	if(!orders.isEmpty()) {
    		sb.append(" ORDER BY ")
    		.forEach(orders.iterator(), SCOMA, o-> o.sql(sb, ctx));
    	}
	}
	
	void fetch(SqlStringBuilder sb) {
		if(currentDatabase() != TERADATA) { // TOP n
			sb.appendIfNonNull(limit,  v-> " LIMIT " + v);
			sb.appendIfNonNull(offset, v-> " OFFSET " + v);
		}
	}

	@Override
	public String toString() {
		return this.build().getQuery();
	}
}

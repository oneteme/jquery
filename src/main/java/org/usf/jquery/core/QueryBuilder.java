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
import static org.usf.jquery.core.Nested.DO_NOTHING;
import static org.usf.jquery.core.QueryContext.parameterized;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Validation.requireNonEmpty;
import static org.usf.jquery.web.ResourceAccessException.resourceAlreadyExistsException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@Getter
public class QueryBuilder {
	
	private final Collection<QueryView> ctes = new ArrayList<>();
	private final Collection<NamedColumn> columns = new ArrayList<>();
	private final Collection<DBColumn> group = new HashSet<>(); 
	private final Collection<DBFilter> where = new ArrayList<>(); 
	private final Collection<DBFilter> having = new ArrayList<>();
	private final Collection<ViewJoin> joins = new ArrayList<>(); 
	private final Collection<DBOrder> orders = new ArrayList<>();
	private final Map<DBView, QueryView> overView = new HashMap<>();
	private boolean distinct;
	@Setter(AccessLevel.PACKAGE)
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
	
	public QueryBuilder ctes(@NonNull QueryView... ctes) {
		addAll(this.ctes, ctes);
		return this;
	}
	
	public QueryBuilder columns(@NonNull NamedColumn... columns) {
		this.clause = COLUMN;
		for(var col : columns) { //optional tag
			if(nonNull(col.getTag()) && this.columns.stream()
					.filter(c-> nonNull(c.getTag()))
					.anyMatch(nc-> nc.getTag().equals(col.getTag()))) {
				throw resourceAlreadyExistsException(col.getTag());
			}
			aggregation |= this.columns.add(col) && col.columns(this, group::add) > 0;
		}
		return this;
	}
	
	public QueryBuilder orders(@NonNull DBOrder... orders) {
		this.clause = ORDER;
		for(var o : orders) {
			aggregation |= this.orders.add(o) && o.columns(this, group::add) > 0;
		}
		return this;
	}

	public QueryBuilder filters(@NonNull DBFilter... filters){
		this.clause = FILTER;
		for(var f : filters) {
			var lvl = f.columns(this, group::add);
			(lvl > 0 ? having : where).add(f);
			aggregation |=  lvl > 0;
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
			sb.runForeach(it, " UNION ALL ", o-> build(sb, pb));
		}
		log.trace("query built in {} ms", currentTimeMillis() - bg);
		return new RequestQuery(sb.toString(), pb.args(), pb.argTypes());
	}

	public final void build(SqlStringBuilder sb, QueryContext ctx){
		with(sb, ctx); //before build
    	select(sb, ctx);
    	var sub = new SqlStringBuilder(500);
    	var frk = ctx.fork();
		join(sub, frk);
    	where(sub, frk);
    	groupBy(sub, frk);
    	having(sub, frk);
    	orderBy(sub, frk);
    	fetch(sub);
		from(sb, ctx); //enumerate all views before from clause
		ctx.join(frk);
		sb.append(sub.toString());
	}
	
	void with(SqlStringBuilder sb, QueryContext ctx) {
		if(!ctes.isEmpty()) {
			sb.append("WITH ")
			.runForeach(ctes.iterator(), SCOMA, v->{
				sb.append(ctx.viewAlias(v)).as();
				v.sql(sb, ctx); //query parenthesis
			})
			.space();
		}
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
    	.runForeach(columns.iterator(), SCOMA, o-> o.sqlUsingTag(sb, ctx));
	}
	
	void from(SqlStringBuilder sb, QueryContext ctx) {
		var views = ctx.views();
		if(!joins.isEmpty()) {
			var exp = joins.stream().map(ViewJoin::getView).toList();
			views = ctx.views().stream().filter(not(exp::contains)).toList();
		}
		if(!views.isEmpty()) {
			sb.append(" FROM ").runForeach(views.iterator(), SCOMA, v-> {
				if(ctes.contains(v)) {
					sb.append(ctx.viewAlias(v));
				}
				else {
					v.sqlUsingTag(sb, ctx);
				}
			});
		}
	}
	
	void join(SqlStringBuilder sb, QueryContext ctx) {
		if(!joins.isEmpty()) {
			sb.space().runForeach(joins.iterator(), SPACE, v-> v.sql(sb, ctx));
		}
	}

	void where(SqlStringBuilder sb, QueryContext ctx){
		if(!where.isEmpty()) {
    		sb.append(" WHERE ").runForeach(where.iterator(), AND.sql(), f-> f.sql(sb, ctx));
		}
	}
	
	void groupBy(SqlStringBuilder sb, QueryContext ctx){
		if(aggregation && !group.isEmpty()) {
    		sb.append(" GROUP BY ").runForeach(group.iterator(), SCOMA, c-> {
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
    		.runForeach(having.iterator(), AND.sql(), f-> f.sql(sb, ctx));
		}
	}
	
	void orderBy(SqlStringBuilder sb, QueryContext ctx) {
    	if(!orders.isEmpty()) {
    		sb.append(" ORDER BY ")
    		.runForeach(orders.iterator(), SCOMA, o-> o.sql(sb, ctx));
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
		return build().getQuery();
	}
}

package org.usf.jquery.core;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.addAll;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Database.TERADATA;
import static org.usf.jquery.core.Database.currentDatabase;
import static org.usf.jquery.core.Database.setCurrentDatabase;
import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.QueryContext.parameterized;
import static org.usf.jquery.core.Role.COLUMN;
import static org.usf.jquery.core.Role.FILTER;
import static org.usf.jquery.core.Role.JOIN;
import static org.usf.jquery.core.Role.ORDER;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Validation.requireNonEmpty;
import static org.usf.jquery.web.ResourceAccessException.resourceAlreadyExistsException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
public class RequestComposer {
	
	private final List<QueryView> ctes = new ArrayList<>();
	private final List<NamedColumn> columns = new ArrayList<>();
	private final Set<DBColumn> group = new HashSet<>(); 
	private final List<DBFilter> where = new ArrayList<>(); 
	private final List<DBFilter> having = new ArrayList<>();
	private final List<ViewJoin> joins = new ArrayList<>(); 
	private final List<DBOrder> orders = new ArrayList<>();
	private final Set<DBView> views = new LinkedHashSet<>(); //preserve order
	private boolean distinct;
	private boolean aggregation;
	private Integer limit;
	private Integer offset;
	private Iterator<?> it;
	
	private Role role;
	
	private final Map<DBView, QueryView> overView = new HashMap<>();
	
	public RequestComposer() {
		this(null);
	}
	
	public RequestComposer(Database target) {
		setCurrentDatabase(target);
	}
	
	public RequestComposer ctes(@NonNull QueryView... ctes) {
		addAll(this.ctes, ctes);
		return this;
	}
	
	public RequestComposer columns(@NonNull NamedColumn... columns) {
		this.role = COLUMN;
		for(var col : columns) { //optional tag
			if(nonNull(col.getTag()) && this.columns.stream()
					.filter(c-> nonNull(c.getTag()))
					.anyMatch(nc-> nc.getTag().equals(col.getTag()))) { //tag is null !!
				throw resourceAlreadyExistsException(col.getTag());
			}
			aggregation |= this.columns.add(col) && col.declare(this, group::add) > 0;
		}
		return this;
	}
	
	public RequestComposer orders(@NonNull DBOrder... orders) {
		this.role = ORDER;
		for(var o : orders) {
			aggregation |= this.orders.add(o) && o.declare(this, group::add) > 0;
		}
		return this;
	}

	public RequestComposer filters(@NonNull DBFilter... filters){
		this.role = FILTER;
		for(var f : filters) {
			var arr = new ArrayList<DBColumn>();
			var lvl = f.declare(this, arr::add);
			if(lvl > 0) {
				aggregation |= having.add(f);
				group.addAll(arr);
			}
			else {
				where.add(f);
			}
		}
		return this;
	}

	public RequestComposer joins(@NonNull ViewJoin... joins) {
		this.role = JOIN;
		for(var j : joins) {
			j.declare(this, v->{}); //declare views only, no aggregation
			this.joins.add(j);
		}
		return this;
	}
	
	RequestComposer from(@NonNull DBView... views) {
		addAll(this.views, views);
		return this;
	}
	
	public RequestComposer limit(Integer limit) {
		this.limit = limit;
		return this;
	}
	
	public RequestComposer offset(Integer offset) {
		this.offset = offset;
		return this;
	}
	
	public RequestComposer distinct() {
		distinct = true;
		return this;
	}
	
	public RequestComposer repeat(@NonNull Iterator<?> it) {
		this.it = it;
		return this;
	}
	
	public QueryView asView() {
		return new QueryView(this);
	}
	
	public RequestComposer overView(DBView view, RequestComposer builder) {
		var query = new QueryView(builder);
		query.setCallback((ctx, sub)-> {
			if(views.contains(view)) {
				views.remove(view);
				views.add(query);
			} //else unused CTE
			ctx.viewProxy(view, query);
			overView.put(view, query);
		});
		return ctes(query);
	}
	
	public Query build(){
		return build(null);
	}

	public Query build(String schema) {
		log.trace("building query...");
    	requireNonEmpty(columns, "columns");
		var bg = currentTimeMillis();
		var pb = parameterized(schema, ctes, views); //over clause
		var sb = new SqlStringBuilder(1000); //avg
		if(isNull(it)) {
			build(sb, pb);
		}
		else {
			sb.runForeach(it, " UNION ALL ", o-> build(sb, pb));
		}
		log.trace("query built in {} ms", currentTimeMillis() - bg);
		return new Query(sb.toString(), pb.args());
	}

	public final void build(SqlStringBuilder sb, QueryContext ctx){
		with(sb, ctx); //before build
    	select(sb, ctx);
		from(sb, ctx); //enumerate all views before from clause
		join(sb, ctx);
    	where(sb, ctx);
    	groupBy(sb, ctx);
    	having(sb, ctx);
    	orderBy(sb, ctx);
    	fetch(sb);
	}
	
	void with(SqlStringBuilder sb, QueryContext ctx) {
		if(!ctes.isEmpty()) {
			sb.append("WITH ")
			.runForeach(ctes.iterator(), SCOMA, v-> v.sql(sb.appendAs(), ctx)) //query parenthesis
			.appendSpace();
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
    	.appendSpace()
    	.runForeach(columns.iterator(), SCOMA, o-> o.sqlUsingTag(sb, ctx));
	}
	
	void from(SqlStringBuilder sb, QueryContext ctx) {
		var from = views;
		if(!joins.isEmpty()) {
			joins.stream() //exclude join views
			.map(ViewJoin::getView)
			.forEach(v-> from.remove(overView.containsKey(v) ? overView.get(v) : v));
		}
		if(!from.isEmpty()) {
			sb.append(" FROM ").runForeach(from.iterator(), SCOMA, v-> v.sqlUsingTag(sb, ctx));
		}
	}
	
	void join(SqlStringBuilder sb, QueryContext ctx) {
		if(!joins.isEmpty()) {
			sb.appendSpace().runForeach(joins.iterator(), SPACE, v->{
				if(overView.containsKey(v.getView())) {
					var cte = overView.get(v.getView()); 
					v = v.map((s, c)-> s.append(c.viewAlias(cte)));
				}
				v.sql(sb, ctx);
			});
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

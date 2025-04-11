package org.usf.jquery.core;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.addAll;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.DBColumn.allColumns;
import static org.usf.jquery.core.Database.TERADATA;
import static org.usf.jquery.core.Database.currentDatabase;
import static org.usf.jquery.core.Database.setCurrentDatabase;
import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.QueryBuilder.parameterized;
import static org.usf.jquery.core.Role.COLUMN;
import static org.usf.jquery.core.Role.FILTER;
import static org.usf.jquery.core.Role.JOIN;
import static org.usf.jquery.core.Role.ORDER;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;
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
public class QueryComposer {
	
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
	
	public QueryComposer() {
		this(null);
	}
	
	public QueryComposer(Database target) {
		setCurrentDatabase(target);
	}
	
	public QueryComposer ctes(@NonNull QueryView... ctes) {
		addAll(this.ctes, ctes);
		return this;
	}
	
	public QueryComposer columns(@NonNull NamedColumn... columns) {
		this.role = COLUMN;
		for(var col : columns) { //optional tag
			if(nonNull(col.getTag()) && this.columns.stream()
					.filter(c-> nonNull(c.getTag()))
					.anyMatch(nc-> nc.getTag().equals(col.getTag()))) { //tag is null !!
				throw resourceAlreadyExistsException(col.getTag());
			}
			aggregation |= this.columns.add(col) && col.compose(this, group::add) > 0;
		}
		return this;
	}
	
	public QueryComposer orders(@NonNull DBOrder... orders) {
		this.role = ORDER;
		for(var o : orders) {
			aggregation |= this.orders.add(o) && o.compose(this, group::add) > 0;
		}
		return this;
	}

	public QueryComposer filters(@NonNull DBFilter... filters){
		this.role = FILTER;
		for(var f : filters) {
			var arr = new ArrayList<DBColumn>();
			var lvl = f.compose(this, arr::add);
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

	public QueryComposer joins(@NonNull ViewJoin... joins) {
		this.role = JOIN;
		for(var j : joins) {
			j.compose(this, v->{}); //declare views only, no aggregation
			this.joins.add(j);
		}
		return this;
	}
	
	QueryComposer declare(@NonNull DBView... views) {
		addAll(this.views, views);
		return this;
	}
	
	public QueryComposer limit(Integer limit) {
		this.limit = limit;
		return this;
	}
	
	public QueryComposer offset(Integer offset) {
		this.offset = offset;
		return this;
	}
	
	public QueryComposer distinct() {
		distinct = true;
		return this;
	}
	
	public QueryComposer repeat(@NonNull Iterator<?> it) {
		this.it = it;
		return this;
	}
	
	public QueryView asView() {
		return new QueryView(this);
	}

	public ViewColumn overColumnView(DBColumn column, String tag) {
		var over = new QueryComposer().columns(column.as(tag), allColumns()).asView();
		overView(over);
		return new ViewColumn(tag, over, column.getType(), null);
	}
	
	public QueryComposer overView(QueryView over) {
		var subViews = over.getComposer().getViews();
		if(subViews.size() == 1) {
			var view = subViews.iterator().next();
			return overView(view, over);
		} //else 
		throw new IllegalStateException("view required");
	}

	public QueryComposer overView(DBView view, QueryView over) {
		overView.put(view, over);
		return ctes(over);
	}
	
	public Query compose(){
		return compose(null);
	}

	public Query compose(String schema) {
		log.trace("building query...");
    	requireNonEmpty(columns, "columns");
		var bg = currentTimeMillis();
		overView.forEach((v,o)->{
			if(views.remove(v)) {
				views.add(o);
			}
		});
		var pb = parameterized(schema, ctes, views, overView); //over clause
		if(isNull(it)) {
			build(pb);
		}
		else {
			pb.append(" UNION ALL ", it, o-> build(pb));
		}
		log.trace("query built in {} ms", currentTimeMillis() - bg);
		return pb.build();
	}

	public final void build(QueryBuilder builder){
		with(builder); //before build
    	select(builder);
		from(builder); //enumerate all views before from clause
		join(builder);
    	where(builder);
    	groupBy(builder);
    	having(builder);
    	orderBy(builder);
    	fetch(builder);
	}
	
	void with(QueryBuilder builder) {
		if(!ctes.isEmpty()) {
			builder.append("WITH ")
			.append(SCOMA, ctes.iterator(), v-> builder.appendViewAlias(v).appendAs().append(v)) //query parenthesis
			.appendSpace();
		}
	}

	void select(QueryBuilder builder){
		if(currentDatabase() == TERADATA) {
			if(nonNull(offset)) {
				throw new UnsupportedOperationException("");
			}
			if(distinct && nonNull(limit)) {
				throw new UnsupportedOperationException("Top N option is not supported with DISTINCT option.");
			}
		}
		builder.append("SELECT");
		if(distinct) {
			builder.append(" DISTINCT");
		}
    	if(nonNull(limit) && currentDatabase() == TERADATA){
    		builder.append(" TOP " + limit);
    	}
    	builder.appendSpace().append(SCOMA, columns.iterator(), o-> {
    		builder.append(o);
    		var tag = o.getTag();
    		if(nonNull(tag)) {
    			builder.appendAs().append(doubleQuote(tag));
    		}
    	});
	}
	
	void from(QueryBuilder query) {
		var from = views;
		if(!joins.isEmpty()) {
			joins.stream() //exclude join views
			.map(ViewJoin::getView)
			.forEach(v-> from.remove(overView.containsKey(v) ? overView.get(v) : v));
		}
		if(!from.isEmpty()) {
			query.append(" FROM ").append(SCOMA, from.iterator(), v-> {
				if(!ctes.contains(v)) {
					query.append(v).appendSpace();
				}
				query.appendViewAlias(v);
			});
		}
	}
	
	void join(QueryBuilder builder) {
		if(!joins.isEmpty()) {
			builder.appendSpace().append(SPACE, joins.iterator(), v-> {
				if(overView.containsKey(v.getView())) {
					var cte = overView.get(v.getView()); 
					v = v.map(c-> c.appendViewAlias(cte));
				}
				v.build(builder);
			});
		}
	}

	void where(QueryBuilder builder){
		if(!where.isEmpty()) {
    		builder.append(" WHERE ").append(AND.sql(), where.iterator());
		}
	}
	
	void groupBy(QueryBuilder builder){
		if(aggregation && !group.isEmpty()) {
    		builder.append(" GROUP BY ").append(SCOMA, group.iterator(), c-> {
    			if(!(c instanceof ViewColumn) && columns.contains(c)) {
    				builder.append(((NamedColumn)c).getTag());
    			}
    			else {
    				 c.build(builder);
    			}
    		});
		}
	}
	
	void having(QueryBuilder builder){
		if(!having.isEmpty()) {
    		builder.append(" HAVING ").append(AND.sql(), having.iterator());
		}
	}
	
	void orderBy(QueryBuilder builder) {
    	if(!orders.isEmpty()) {
    		builder.append(" ORDER BY ").append(SCOMA, orders.iterator());
    	}
	}
	
	void fetch(QueryBuilder builder) {
		if(currentDatabase() != TERADATA) { // TOP n
			if(nonNull(limit)) {
				builder.append(" LIMIT " + limit);
			}
			if(nonNull(offset)) {
				builder.append(" OFFSET " + offset);
			}
		}
	}

	@Override
	public String toString() {
		return compose().getSql();
	}
}

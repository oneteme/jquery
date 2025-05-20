package org.usf.jquery.core;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.addAll;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
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
import static org.usf.jquery.core.Role.UNION;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;
import static org.usf.jquery.core.Validation.requireNonEmpty;
import static org.usf.jquery.web.ResourceAccessException.resourceAlreadyExistsException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
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
public class QueryComposer {
	
	static final Consumer<DBColumn> DO_NOTHING = o->{};
	
	private final List<QueryView> ctes = new ArrayList<>();
	private final List<NamedColumn> columns = new ArrayList<>();
	private final Set<DBColumn> group = new HashSet<>(); 
	private final List<DBFilter> where = new ArrayList<>(); 
	private final List<DBFilter> having = new ArrayList<>();
	private final List<ViewJoin> joins = new ArrayList<>(); 
	private final List<DBOrder> orders = new ArrayList<>();
	private final Set<DBView> views = new LinkedHashSet<>(); //preserve order
	private final List<QueryUnion> unions = new ArrayList<>();
	private final Map<String, String[]> variables = new LinkedHashMap<>();
	private boolean distinct;
	private boolean aggregation;
	private Integer limit;
	private Integer offset;
	private Object[] drivenModel;
	
	private Role role;
	
	private final Map<DBView, QueryView> overView = new HashMap<>();
	
	public QueryComposer() {
		this(null);
	}
	
	public QueryComposer(Database target) {
		setCurrentDatabase(target);
	}
	
	public String[] getVariables(String key){
		return variables.get(key);
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
			j.compose(this, DO_NOTHING); //declare views only, no aggregation
			this.joins.add(j);
		}
		return this;
	}
	
	public QueryComposer unions(@NonNull QueryUnion... unions) {
		this.role = UNION;
		for(var o : unions) {
			o.compose(this, DO_NOTHING); //declare views only, no aggregation
			this.unions.add(o);
		}
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
	
	public QueryComposer distinct(boolean distinct) {
		this.distinct = distinct;
		return this;
	}
	
	public <T> QueryComposer repeat(@NonNull T[] drivenModel) {
		this.drivenModel = drivenModel;
		return this;
	}
	
	QueryComposer declare(@NonNull DBView... views) {
		for(var v : views) {
			if(this.views.add(v)) {
				v.compose(this, DO_NOTHING); //look for nested ctes
			}
		}
		return this;
	}

	public QueryView subQuery(DBView view) {
		return subQuery(view,
				()-> new QueryComposer().columns(allColumns(view)).asView());
	}
	
	public QueryView subQuery(DBView view, Supplier<QueryView> orElse) {
		var sub = overView.computeIfAbsent(view, v-> requireNonNull(orElse.get(), "subQuery is null"));
		ctes(sub);
		return sub;
	}
	
	public QueryView asView() {
		return new QueryView(this);
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
				log.trace("{} => {}", v, o);
			}
		});
		var builder = parameterized(schema, ctes, views, overView);
		with(builder); //before build
		build(builder);
		log.trace("query built in {} ms", currentTimeMillis() - bg);
		return builder.build();
	}
	
	public final void build(QueryBuilder builder){
		if(isNull(drivenModel)) {
			internalBuild(builder);
		}
		else {
			builder.appendEach(" UNION ALL ", drivenModel, o-> internalBuild(builder.withModel(o)));
		}
	}
	
	private void internalBuild(QueryBuilder builder) {
		select(builder);
		from(builder);
		join(builder);
    	where(builder);
    	groupBy(builder);
    	having(builder);
    	orderBy(builder);
    	fetch(builder);
    	union(builder);
	}
	
	void with(QueryBuilder builder) {
		if(!ctes.isEmpty()) {
			builder.append("WITH ")
			.appendEach(SCOMA, ctes, v-> builder.appendViewAlias(v).appendAs().append(v)) //query parenthesis
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
		builder.append("SELECT ");
		if(distinct) {
			builder.append("DISTINCT ");
		}
    	if(nonNull(limit) && currentDatabase() == TERADATA){
    		builder.append("TOP " + limit);
    	}
    	builder.appendEach(SCOMA, columns, o-> {
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
			query.append(" FROM ").appendEach(SCOMA, from, v-> {
				if(!ctes.contains(v)) {
					query.append(v).appendSpace();
				}
				query.appendViewAlias(v);
			});
		}
	}
	
	void join(QueryBuilder builder) {
		if(!joins.isEmpty()) {
			builder.appendSpace().appendEach(SPACE, joins, v-> {
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
    		builder.append(" WHERE ").appendEach(AND.sql(), where);
		}
	}
	
	void groupBy(QueryBuilder builder){
		if(aggregation && !group.isEmpty()) {
    		builder.append(" GROUP BY ").appendEach(SCOMA, group, c-> {
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
    		builder.append(" HAVING ").appendEach(AND.sql(), having);
		}
	}
	
	void orderBy(QueryBuilder builder) {
    	if(!orders.isEmpty()) {
    		builder.append(" ORDER BY ").appendEach(SCOMA, orders);
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

	void union(QueryBuilder builder) {
    	if(!unions.isEmpty()) {
    		builder.appendSpace().appendEach(SPACE, unions);
    	}
	}
	
	@Override
	public String toString() {
		return compose().getSql();
	}
}

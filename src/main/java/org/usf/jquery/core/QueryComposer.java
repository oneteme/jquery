package org.usf.jquery.core;

import static java.lang.System.currentTimeMillis;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static org.usf.jquery.core.DBColumn.allColumns;
import static org.usf.jquery.core.Environment.NO_ENV;
import static org.usf.jquery.core.Role.COLUMN;
import static org.usf.jquery.core.Role.FILTER;
import static org.usf.jquery.core.Role.JOIN;
import static org.usf.jquery.core.Role.ORDER;
import static org.usf.jquery.core.Role.UNION;
import static org.usf.jquery.core.Utils.computeIfAbsentElseThrow;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.MessageUtils.resourceAlreadyExistsMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;

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
public final class QueryComposer {
	
	static final Consumer<DBColumn> DO_NOTHING = o->{};
	
	private final Set<QueryView> ctes = new LinkedHashSet<>();
	private final List<NamedColumn> columns = new ArrayList<>();
	private final Set<DBView> views = new LinkedHashSet<>(); //preserve order
	private final List<ViewJoin> joins = new ArrayList<>(); 
	private final Set<DBColumn> group = new HashSet<>(); 
	private final List<DBFilter> where = new ArrayList<>(); 
	private final List<DBFilter> having = new ArrayList<>();
	private final List<DBOrder> orders = new ArrayList<>();
	private final List<QueryUnion> unions = new ArrayList<>();
	private final Map<String, String[]> variables = new LinkedHashMap<>();
	private boolean distinct;
	private boolean aggregation;
	private Integer limit;
	private Integer offset;
	private Object[] drivenModel;
	
	private final Map<DBView, QueryComposer> overView = new HashMap<>();
	
	private Role role;
	
	private final QueryView queryView = new QueryView(); //assume unique reference

	public String[] getVariables(String key){
		return variables.get(key);
	}
	
	public QueryComposer ctes(@NonNull QueryView... ctes) {
		for(var c : ctes) {
			this.ctes.add(c); //unnecessary compose
		}
		return this;
	}
	
	public QueryComposer columns(@NonNull NamedColumn... columns) {
		this.role = COLUMN;
		for(var col : columns) { //optional tag
			if(nonNull(col.getTag()) && this.columns.stream()
					.filter(c-> nonNull(c.getTag()))
					.anyMatch(nc-> nc.getTag().equals(col.getTag()))) { //tag is null !!
				throw new IllegalArgumentException(resourceAlreadyExistsMessage("column", col.getTag()));
			}
			aggregation |= this.columns.add(col) && col.compose(this, group::add) > 0;
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
	

	public QueryComposer joins(@NonNull JoinsClause... joins) {
		for(var j : joins) {
			this.joins(j.getJoins()); //TODO check 
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
	
	public QueryComposer orders(@NonNull DBOrder... orders) {
		this.role = ORDER;
		for(var o : orders) {
			aggregation |= this.orders.add(o) && o.compose(this, group::add) > 0;
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
	
	public QueryComposer variable(@NonNull String key, String... values) {
		variables.compute(key, computeIfAbsentElseThrow(values, ()-> resourceAlreadyExistsMessage("variable", key)));
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
	
	public QueryView getSubView(DBView view) {
		var sub = overView.get(view);
		return nonNull(sub) ? sub.getQueryView() : null;
	}

	public QueryComposer subViewQuery(DBView view, Consumer<QueryComposer> consumer) {
		return subViewQuery(view, true, consumer);
	}

	public QueryComposer subViewQuery(DBView view, boolean allColumn, Consumer<QueryComposer> consumer) {
		var q = overView.computeIfAbsent(view, k-> {
			var sub = new QueryComposer();
			if(allColumn) {
				sub.columns(allColumns(k));
			}
			ctes(sub.getQueryView());
			return sub;
		});
		consumer.accept(q);
		q.compose(); //!important compose sub query each time after change
		return this;
	}
	
	@Deprecated(forRemoval = true, since = "v4")
	public Query build(){
		return compose().buildQuery(NO_ENV, true, drivenModel);
	}
	
	public QueryView compose() { //TD check this[clause].length = QueryView[clause].length
		log.trace("composing query...");
		var bg = currentTimeMillis();
		if(!isEmpty(columns)) {
			acceptArray(columns, NamedColumn[]::new, queryView::setColumns);
		}
		else {
			throw new IllegalArgumentException("no columns defined in query");
		}
		acceptArray(ctes, QueryView[]::new, queryView::setCtes);
		acceptArray(views, DBView[]::new, queryView::setViews);
		acceptArray(joins, ViewJoin[]::new, queryView::setJoins);
		acceptArray(where, DBFilter[]::new, queryView::setWhere);
		acceptArray(group, DBColumn[]::new, queryView::setGroup);
		acceptArray(having, DBFilter[]::new, queryView::setHaving);
		acceptArray(orders, DBOrder[]::new, queryView::setOrders);
		acceptArray(unions, QueryUnion[]::new, queryView::setUnions);
		acceptObject(limit, Objects::nonNull, queryView::setLimit);
		acceptObject(offset, Objects::nonNull, queryView::setOffset);
		if(isDistinct()) {
			queryView.setDistinct(true);
		}
		if(isAggregation()) {
			queryView.setAggregation(true);
		}
		queryView.setOverView(overView.entrySet().stream()
				.collect(toMap(Entry::getKey, v-> v.getValue().getQueryView()))); 
		log.trace("query composed in {} ms", currentTimeMillis() - bg);
		return queryView;
	}

	@Override
	public String toString() {
		return compose().toString();
	}
	
	static <T> void acceptArray(Collection<T> arr, IntFunction<T[]> generator, Consumer<T[]> consumer) {
		if(!isEmpty(arr)) {
			consumer.accept(arr.stream().toArray(generator));
		}
	}

	static <T> void acceptObject(T o, Predicate<T> pre, Consumer<T> consumer) {
		if(pre.test(o)) {
			consumer.accept(o);
		}
	}
}

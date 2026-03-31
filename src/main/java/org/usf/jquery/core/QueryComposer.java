package org.usf.jquery.core;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.addAll;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static org.usf.jquery.core.Column.allColumns;
import static org.usf.jquery.core.DBObject.DECLARE_ONLY;
import static org.usf.jquery.core.DBObject.composeNested;
import static org.usf.jquery.core.Role.COLUMN;
import static org.usf.jquery.core.Role.CRITERIA;
import static org.usf.jquery.core.Role.JOIN;
import static org.usf.jquery.core.Role.ORDER;
import static org.usf.jquery.core.Role.UNION;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.MessageUtils.resourceAlreadyExistsMessage; //TODO move to core package	

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
public final class QueryComposer implements Composer<QueryView> {
	
	private final List<NamedColumn> columns = new ArrayList<>();
	private Set<DBView> ctes; //views
	private List<ViewJoin> joins; 
	private Set<Column> groups; 
	private List<Criteria> where; 
	private List<Order> orders;
	private List<QueryUnion> unions;
	private boolean distinct;
	private boolean aggregation;
	private int limit  = -1;
	private int offset = -1;
	private int maxRows = -1; //security
	
	private final Map<DBView, QueryComposer> overView = new HashMap<>();
	
	private Role role;
	
	private final QueryView queryView = new QueryView(); //assume unique reference

	public QueryComposer ctes(QueryView... ctes) {
		if(!isEmpty(ctes)) {
			if(isNull(this.ctes)) {
				this.ctes = new HashSet<>();
			}
			addAll(this.ctes, ctes);
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
			aggregation |= this.columns.add(col) && col.compose(this, groups::add) > 0;
		}
		return this;
	}


	public QueryComposer criterias(Criteria... criterias){
		if(!isEmpty(criterias)) {
			if(isNull(this.where)) {
				this.where = new ArrayList<>();
			}
			addAll(this.where, criterias);
		}
		return this;
	}
	
	public QueryComposer groups(Column... columns) {
		if(!isEmpty(columns)) {
			if(isNull(this.groups)) {
				this.groups = new HashSet<>();
			}
			addAll(this.groups, columns);
		}
		return this;
	}

	public QueryComposer joins2(@NonNull JoinsClause... joins) {
		for(var j : joins) {
			this.joins(j.getJoins()); //TODO check 
		}
		return this;
	}

	public QueryComposer joins(@NonNull ViewJoin... joins) {
		this.role = JOIN;
		for(var j : joins) {
			j.compose(this, DECLARE_ONLY); //declare views only, no aggregation
			this.joins.add(j);
		}
		return this;
	}
	
	public QueryComposer orders(Order... orders) {
		if(!isEmpty(orders)) {
			if(isNull(this.orders)) {
				this.orders = new ArrayList<>();
			}
			addAll(this.orders, orders);
		}
		return this;
	}
	
	public QueryComposer unions(@NonNull QueryUnion... unions) {
		if(!isEmpty(unions)) {
			if(isNull(this.unions)) {
				this.unions = new ArrayList<>();
			}
			addAll(this.unions, unions);
		}
		return this;
	}
	
	public QueryComposer limit(int limit) {
		this.limit = limit;
		return this;
	}
	
	public QueryComposer offset(int offset) {
		this.offset = offset;
		return this;
	}
	
	public QueryComposer distinct(boolean distinct) {
		this.distinct = distinct;
		return this;
	}
	
	public QueryComposer maxRows(int maxRows) {
		if(maxRows < 1) {
			throw new IllegalArgumentException("max rows must be greater than 0");
		}
		this.maxRows = maxRows;
		return this;
	}
	
	QueryComposer declare(@NonNull DBView... views) {
		for(var v : views) {
			if(this.views.add(v)) {
				v.compose(this, DECLARE_ONLY); //look for nested ctes
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
		acceptArray(where, Criteria[]::new, queryView::setWhere);
		acceptArray(groups, Column[]::new, queryView::setGroup);
		acceptArray(having, Criteria[]::new, queryView::setHaving);
		acceptArray(orders, Order[]::new, queryView::setOrders);
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
	
	public QueryView compose2() {
		var query = new QueryView();
		if(distinct) {
			query.setDistinct(distinct);
		}
		if(limit > -1) {
			query.setLimit(limit);
		}
		if(offset > -1) {
			query.setOffset(offset);
		}
		var aggStr = DECLARE_ONLY;
		if(isNull(groups)) { //!explicit
			groups = new HashSet<>();
			aggStr = groups::add;
		}
		composeColumn(query, aggStr);
		composeJoin(query);
		composeCriteria(query, aggStr); //where | having
		composeOrder(query, aggStr);
		composeUnion(query);
		composeView(query);
		return query;
	}
	
	void composeView(QueryView query) {
		Map<DBView, QueryView> map = null;
		for(var view : ctes) {
			if(view instanceof SubView sub) {
				if(isNull(map)) {
					map = new LinkedHashMap<>();
				}
				var over = sub.getQuery();
				for(var s : over.getViews()) {
					map.compute(s, (k,v)->{
						if(isNull(v) || v == over) {
							return over;
						}
						throw new IllegalStateException();
					});
				}
			}
		}
		if(nonNull(map)) {
			query.setOverView(map);
		}
	}
	
	void composeColumn(QueryView view, Consumer<Column> aggStr) {
		if(!columns.isEmpty()) {
			var cols = columns.toArray(NamedColumn[]::new);
			composeNested(this, aggStr, cols);
			view.setColumns(cols);
		}
		else {
			throw new IllegalArgumentException("no columns defined in query");
		}
	}
	
	void composeJoin(QueryView view) {
		if(nonNull(joins)) {
			var jns = joins.toArray(ViewJoin[]::new);
			composeNested(this, DECLARE_ONLY, jns);
			view.setJoins(jns);
		}
	}

	void composeCriteria(QueryView view, Consumer<Column> aggStr) {
		if(nonNull(where)) {
			Set<Criteria> hvn = null;
			for(var c : where) {
				if(c.compose(this, aggStr) > 0) {
					if(isNull(hvn)) {
						hvn = new HashSet<>();
					}
					hvn.add(c);
				}
			}
			if(nonNull(hvn)) {
				view.setHaving(hvn.toArray(Criteria[]::new));
				where.removeAll(hvn);
			}
			view.setWhere(where.toArray(Criteria[]::new));
		}
	}
	
	void composeOrder(QueryView view, Consumer<Column> aggStr) {
		if(nonNull(orders)) {
			var ords = orders.toArray(Order[]::new);
			composeNested(this, aggStr, ords);
			view.setOrders(ords);
		}
	}
	
	void composeUnion(QueryView view) {
		if(nonNull(unions)) {
			view.setUnions(unions.toArray(QueryUnion[]::new)); 
		}
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

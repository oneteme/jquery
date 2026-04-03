package org.usf.jquery.core;

import static java.lang.Math.max;
import static java.util.Collections.addAll;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Column.allColumns;
import static org.usf.jquery.core.QueryDeclaration.DECLARE_ONLY;
import static org.usf.jquery.core.Role.COLUMN;
import static org.usf.jquery.core.Utils.appendLast;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.MessageUtils.resourceAlreadyExistsMessage; //TODO move to core package	

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

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
	private Set<QueryView> ctes; //views
	private List<ViewJoin> joins; 
	private Set<Column> groups; 
	private List<Criteria> criterias; 
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
			this.columns.add(col);
		}
		return this;
	}


	public QueryComposer criterias(Criteria... criterias){
		if(!isEmpty(criterias)) {
			if(isNull(this.criterias)) {
				this.criterias = new ArrayList<>();
			}
			addAll(this.criterias, criterias);
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
			this.joins(j.getJoins());
		}
		return this;
	}

	public QueryComposer joins(@NonNull ViewJoin... joins) {
		if(!isEmpty(joins)) {
			if(isNull(this.joins)) {
				this.joins = new ArrayList<>();
			}
			addAll(this.joins, joins);
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
		if(maxRows < 0) {
			throw new IllegalArgumentException("max rows must be greater than 0");
		}
		this.maxRows = maxRows;
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
	
	@Override
	public String toString() {
		return compose().toString();
	}
	
	public QueryView compose() {
		var query = new QueryView();
		Consumer<Column> cons = DECLARE_ONLY;
		if(isNull(groups)) { //!explicit
			groups = new HashSet<>();
			cons = groups::add;
		}
		int mask = -1;
		var declare = new QueryDeclaration(new HashSet<>(), cons);
		mask = max(composeColumn(query, declare), mask);
		mask = max(composeCriteria(query, declare), mask); //where | having
		mask = max(composeOrder(query, declare), mask);
		composeJoin(query, declare);
		composeUnion(query);
		composeView(query, declare);
		if(mask > 0 && !isEmpty(groups)) {			
			query.setGroups(groups.toArray(Column[]::new));
		}
		if(!isEmpty(declare.getViews())) {	
			query.setViews(declare.getViews().toArray(DBView[]::new));
		}
		if(distinct) {
			query.setDistinct(distinct);
		}
		if(limit > -1) {
			query.setLimit(limit);
		}
		if(offset > -1) {
			query.setOffset(offset);
		}
		return query;
	}
	
	void composeView(QueryView query, QueryDeclaration declare) {
		List<DBView> from = null;
		Map<DBView, QueryView> map = null;
		for(var view : declare.getViews()) {
			if(view instanceof SubView sub) {
				if(isNull(map)) {
					map = new LinkedHashMap<>();
				}
				var over = sub.getQuery();
				for(var s : over.getViews()) {
					map.compute(s, (k,v)->{
						if(isNull(v) || v == over) {
							return over;
						}//else merge
						throw new IllegalStateException();
					});
				}
				if(!isEmpty(over.getViews()) && !isEmpty(joins)) {
					var set = Set.of(over.getViews());
					for(var it=joins.listIterator(); it.hasNext();) {
						var v = it.next();
						if(set.contains(v.getView())) {
							over.setJoins(appendLast(over.getJoins(), v));
							it.remove();
						}
					}
				}
			}
			else {
				if(isNull(from)) {
					from = new ArrayList<>();
				}
				from.add(view);
			}
		}
		if(nonNull(map)) {
			query.setOverView(map);
		}
		if(nonNull(from)) {
			query.setViews(from.toArray(DBView[]::new));
		}
	}
	
	int composeColumn(QueryView view, QueryDeclaration declare) {
		if(!columns.isEmpty()) {
			var cols = columns.toArray(NamedColumn[]::new);
			view.setSelects(cols);
			return declare.composeNested(cols);
		}
		throw new IllegalArgumentException("no columns defined in query");
	}
	
	void composeJoin(QueryView view, QueryDeclaration declare) {
		if(nonNull(joins)) {
			var jns = joins.toArray(ViewJoin[]::new);
			view.setJoins(jns);
			declare.sub(DECLARE_ONLY).composeNested(jns);
		}
	}

	int composeCriteria(QueryView view, QueryDeclaration declare) {
		var mask = -1;
		if(nonNull(criterias)) {
			Set<Criteria> hvn = null;
			for(var c : criterias) {
				var v = c.compose(declare);
				if(v > 0) {
					if(isNull(hvn)) {
						hvn = new HashSet<>();
					}
					hvn.add(c);
				}
				mask = max(mask, v);
			}
			if(nonNull(hvn)) {
				view.setHavings(hvn.toArray(Criteria[]::new));
				criterias.removeAll(hvn);
			}
			view.setWheres(criterias.toArray(Criteria[]::new));
		}
		return mask;
	}
	
	int composeOrder(QueryView view, QueryDeclaration declare) {
		if(nonNull(orders)) {
			var ords = orders.toArray(Order[]::new);
			view.setOrders(ords);
			return declare.composeNested(ords);
		}
		return -1;
	}
	
	void composeUnion(QueryView view) {
		if(nonNull(unions)) {
			view.setUnions(unions.toArray(QueryUnion[]::new)); 
		}
	}
	
}

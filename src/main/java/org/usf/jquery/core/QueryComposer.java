package org.usf.jquery.core;

import static java.lang.Math.max;
import static java.util.Collections.addAll;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.DBObject.MEASURE;
import static org.usf.jquery.core.DBObject.SCALAR;
import static org.usf.jquery.core.QueryManifest.Section.COLUMN;
import static org.usf.jquery.core.QueryManifest.Section.CRITERIA;
import static org.usf.jquery.core.QueryManifest.Section.ORDER;
import static org.usf.jquery.core.Stores.NO_STORE;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public final class QueryComposer implements Composer<QueryView> {
	
	private final List<Column> columns = new ArrayList<>();
	private Set<QueryView> ctes; //views
	private List<ViewJoin> joins; 
	private List<Criteria> criterias;
	private List<Order> orders;
	private List<QueryUnion> unions;
	@Setter	private Set<Column> groups; //explicit group-by columns
	@Setter private Set<DBView> froms; // explicit from views
	private boolean distinct;
	private boolean aggregation; //check this
	private int limit  = -1;
	private int offset = -1;
	private int maxRows = -1; //security
	
	private final Map<DBView, QueryView> overView = new HashMap<>();

	public QueryComposer ctes(QueryView... ctes) {
		if(!isEmpty(ctes)) {
			if(isNull(this.ctes)) {
				this.ctes = new HashSet<>();
			}
			addAll(this.ctes, ctes);
		}
		return this;
	}
	
	public QueryComposer columns(NamedColumn... columns) {
		if(!isEmpty(columns)) {
			addAll(this.columns, columns);
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
		if(!isEmpty(joins)) {
			for(var j : joins) {
				this.joins(j.getJoins());
			}
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
	
	public QueryComposer unions(QueryUnion... unions) {
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
		this.maxRows = maxRows;
		return this;
	}
	
//	public QueryView getSubView(DBView view) {
//		var sub = overView.get(view);
//		return nonNull(sub) ? sub.getQueryView() : null;
//	}
//
//	@Deprecated
//	public QueryComposer subViewQuery(DBView view, Consumer<QueryComposer> consumer) {
//		return subViewQuery(view, true, consumer);
//	}
//
//	@Deprecated
//	public QueryComposer subViewQuery(DBView view, boolean allColumn, Consumer<QueryComposer> consumer) {
//		var q = overView.computeIfAbsent(view, k-> {
//			var sub = new QueryComposer();
//			if(allColumn) {
//				sub.columns(allColumns(k));
//			}
//			ctes(sub.getQueryView());
//			return sub;
//		});
//		consumer.accept(q);
//		q.compose(); //!important compose sub query each time after change
//		return this;
//	}
	
	@Override
	public QueryView compose(Store store) {
		return compose(new QueryView(store));
	}
	
	QueryView compose(QueryView view) {
		var manf = new QueryManifest(view.getStore(), ctes, froms, groups, overView);
		var aggr = composeColumn(view, manf);
		aggr = max(composeCriteria(view, manf), aggr); //where & having
		aggr = max(composeOrder(view, manf), aggr);
		composeGroupBy(view, manf, aggr);
		composeJoin(view);
		composeUnion(view);
		composeCte(view, manf);
		composeFrom(view, manf);
		view.setDistinct(distinct);
		view.setLimit(max(-1, limit));
		view.setOffset(max(-1, offset));
		return view;
	}
	
	void composeCte(QueryView view, QueryManifest manifest) {
		if(!isEmpty(manifest.getCtes())) {
			view.setCtes(manifest.getCtes().toArray(QueryView[]::new));
		}
		if(!isEmpty(manifest.getOverViews())) {
			view.setOverView(manifest.getOverViews());
		}
	}
	
	int composeColumn(QueryView view, QueryManifest manifest) {
		if(!isEmpty(columns)) {
			manifest.setRole(COLUMN);
			var cols = columns.toArray(Column[]::new);
			view.setSelects(cols);
			return manifest.prepareNested(cols);
		}
		throw new IllegalArgumentException("no columns defined in query");
	}
	
	void composeFrom(QueryView view, QueryManifest manifest) {
		var views = manifest.getFroms();
		if(!isEmpty(views)) {
			if(manifest.isScanFroms() && nonNull(joins)) { //modifiable set 
				for(var j : joins) {
					views.remove(j.getView());
				}	
			}
			view.setFroms(views.toArray(DBView[]::new));
		}
	}

	int composeCriteria(QueryView view, QueryManifest manifest) {
		var isAgg = SCALAR;
		if(!isEmpty(criterias)) {
			manifest.setRole(CRITERIA);
			List<Criteria> whr = null;
			List<Criteria> hvn = null;
			for(var crt : criterias) {
				var v = crt.prepare(manifest);
				if(v == MEASURE) {
					if(isNull(hvn)) {
						hvn = new ArrayList<>();
					}
					hvn.add(crt);
				}
				else {
					if(isNull(whr)) {
						whr = new ArrayList<>();
					}
					whr.add(crt);
				}
				isAgg = max(isAgg, v);
			}
			if(nonNull(whr)) {
				view.setWheres(whr.toArray(Criteria[]::new));
			}
			if(nonNull(hvn)) {
				view.setHavings(hvn.toArray(Criteria[]::new));
			}
		}
		return isAgg;
	}
	
	int composeOrder(QueryView view, QueryManifest manifest) {
		if(!isEmpty(orders)) {
			manifest.setRole(ORDER);
			var ords = orders.toArray(Order[]::new);
			view.setOrders(ords);
			return manifest.prepareNested(ords);
		}
		return -1;
	}

	void composeGroupBy(QueryView view, QueryManifest manifest, int aggr) {
		if(!isEmpty(manifest.getGroups()) && (aggr == MEASURE || !manifest.isScanGroups())) {			
			view.setGroups(manifest.getGroups().toArray(Column[]::new));
		}
	}
	
	void composeJoin(QueryView view) {
		if(!isEmpty(joins)) {
			view.setJoins(joins.toArray(ViewJoin[]::new));
		}
	}
	
	void composeUnion(QueryView view) {
		if(!isEmpty(unions)) {
			view.setUnions(unions.toArray(QueryUnion[]::new)); 
		}
	}
	
	@Override
	public String toString() {
		return compose(NO_STORE).toString();
	}
}

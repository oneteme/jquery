package org.usf.jquery.core;

import static java.lang.Math.max;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.addAll;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.DBObject.MEASURE;
import static org.usf.jquery.core.DBObject.SCALAR;
import static org.usf.jquery.core.QueryManifest.Section.COLUMN;
import static org.usf.jquery.core.QueryManifest.Section.CRITERIA;
import static org.usf.jquery.core.QueryManifest.Section.CTE;
import static org.usf.jquery.core.QueryManifest.Section.JOIN;
import static org.usf.jquery.core.QueryManifest.Section.ORDER;
import static org.usf.jquery.core.QueryManifest.Section.UNION;
import static org.usf.jquery.core.Stores.NO_STORE;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@RequiredArgsConstructor
public final class QueryComposer implements Composer<QueryView> {
	
	private final List<Column> columns = new ArrayList<>();
	private List<QueryView> ctes;
	private List<Criteria> criterias;
	private List<Order> orders;
	private List<ViewJoin> joins; 
	private List<QueryUnion> unions;
	private List<Column> groups; 
	private List<DBView> froms;
	private boolean distinct;
	private int limit  = -1;
	private int offset = -1;
	private int maxRows = -1; //security
	
	private final Map<DBView, QueryView> overView = new HashMap<>();

	public QueryComposer cte(@NonNull QueryView cte) {
		getCtes().add(cte);
		return this;
	}
	
	public QueryComposer ctes(@NonNull QueryView... ctes) {
		addAll(getCtes(), ctes);
		return this;
	}
	
	public QueryComposer ctes(@NonNull Collection<QueryView> ctes) {
		getCtes().addAll(ctes);
		return this;
	}
	
	private Collection<QueryView> getCtes(){
		if(isNull(ctes)) {
			ctes = new ArrayList<>();
		}
		return ctes;
	}
	
	public QueryComposer column(@NonNull Column column) {
		getColumns().add(column);
		return this;
	}
	
	public QueryComposer columns(@NonNull Column... columns) {
		addAll(getColumns(), columns);
		return this;
	}
	
	public QueryComposer columns(@NonNull Collection<Column> columns) {
		getColumns().addAll(columns);
		return this;
	}
	
	private Collection<Column> getColumns(){
		return columns;
	}
	
	public QueryComposer criteria(@NonNull Criteria criteria){
		getCriterias().add(criteria);
		return this;
	}

	public QueryComposer criterias(@NonNull Criteria... criterias){
		addAll(getCriterias(), criterias);
		return this;
	}
	
	public QueryComposer criterias(@NonNull Collection<Criteria> criterias){
		getCriterias().addAll(criterias);
		return this;
	}
	
	private Collection<Criteria> getCriterias(){
		if(isNull(this.criterias)) {
			this.criterias = new ArrayList<>();
		}
		return criterias;
	}
	
	public QueryComposer group(@NonNull Column group) {
		getGroups().add(group);
		return this;
	}
	
	public QueryComposer groups(@NonNull Column... groups) {
		addAll(getGroups(), groups);
		return this;
	}
	
	public QueryComposer groups(@NonNull Collection<Column> groups) {
		getGroups().addAll(groups);
		return this;
	}
	
	private Collection<Column> getGroups(){
		if(isNull(groups)) {
			groups = new ArrayList<>();
		}
		return groups;
	}
	
	public QueryComposer from(@NonNull DBView view) {
		getFroms().add(view);
		return this;
	}

	public QueryComposer froms(@NonNull DBView... views) {
		addAll(getFroms(), views);
		return this;
	}
	
	public QueryComposer froms(@NonNull Collection<DBView> views) {
		getFroms().addAll(views);
		return this;
	}
	
	private Collection<DBView> getFroms(){
		if(isNull(froms)) {
			froms = new ArrayList<>();
		}
		return froms;
	}
	
	public QueryComposer join(@NonNull ViewJoin join) {
		getJoins().add(join);
		return this;
	}
	
	public QueryComposer joins(@NonNull ViewJoin... joins) {
		addAll(getJoins(), joins);
		return this;
	}
	
	public QueryComposer joins(@NonNull Collection<ViewJoin> joins) {
		getJoins().addAll(joins);
		return this;
	}
	
	private Collection<ViewJoin> getJoins(){
		if(isNull(joins)) {
			joins = new ArrayList<>();
		}
		return joins;
	}
	
	public QueryComposer order(@NonNull Order order) {
		getOrders().add(order);
		return this;
	}
	
	public QueryComposer orders(@NonNull Order... orders) {
		addAll(getOrders(), orders);
		return this;
	}
	
	public QueryComposer orders(@NonNull Collection<Order> orders) {
		getOrders().addAll(orders);
		return this;
	}
	
	private Collection<Order> getOrders(){
		if(isNull(orders)) {
			orders = new ArrayList<>();
		}
		return orders;
	}
	
	public QueryComposer union(@NonNull QueryUnion union) {
		addAll(getUnions(), union);
		return this;
	}
	
	public QueryComposer unions(@NonNull QueryUnion... unions) {
		addAll(getUnions(), unions);
		return this;
	}
	
	public QueryComposer unions(@NonNull Collection<QueryUnion> unions) {
		getUnions().addAll(unions);
		return this;
	}
	
	private Collection<QueryUnion> getUnions(){
		if(isNull(unions)) {
			unions = new ArrayList<>();
		}
		return unions;
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
	
	@Override
	public QueryView compose(Store store) {
		if(!isEmpty(columns)) {
			return compose(new QueryView(store));
		}
		throw new ComposeException("query requires at least one column");
	}
	
	QueryView compose(QueryView view) {
		log.trace("composing query..");
		var ct = currentTimeMillis();
		var nestCtes = new LinkedHashSet<QueryView>();
		var mnf = new QueryManifest(view.getStore(), nestCtes, 
				isNull(froms) ? new LinkedHashSet<>() : null, 
				isNull(groups) ? new LinkedHashSet<>() : null,
				nonNull(joins) ? unmodifiableCollection(joins) : null, //read only
				new LinkedHashMap<>(overView));
		if(nonNull(this.ctes)) {
			mnf.setRole(CTE);
			mnf.prepareNested(this.ctes);
			nestCtes.addAll(this.ctes); //add ctes after their nested ctes
		}
		var aggr = composeColumn(view, mnf);
		aggr = max(composeCriteria(view, mnf), aggr); //where & having
		aggr = max(composeOrder(view, mnf), aggr);
		composeGroupBy(view, mnf, aggr);
		composeJoin(view, mnf);
		composeUnion(view, mnf);
		composeFrom(view, mnf);
		composeCte(view, mnf);
		view.setDistinct(distinct);
		view.setLimit(max(-1, limit));
		view.setOffset(max(-1, offset));
		log.trace("query composed in {}", currentTimeMillis() - ct);
		return view;
	}
	
	int composeColumn(QueryView view, QueryManifest manifest) {
		if(!isEmpty(columns)) {
			view.setSelects(unmodifiableCollection(columns));
			manifest.setRole(COLUMN);
			return manifest.prepareNested(view.getSelects());
		}
		return SCALAR;
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
				view.setWheres(unmodifiableCollection(whr));
			}
			if(nonNull(hvn)) {
				view.setHavings(unmodifiableCollection(hvn));
			}
		}
		return isAgg;
	}
	
	int composeOrder(QueryView view, QueryManifest manifest) {
		if(!isEmpty(orders)) {
			view.setOrders(unmodifiableCollection(orders));
			manifest.setRole(ORDER);
			return manifest.prepareNested(view.getOrders());
		}
		return SCALAR;
	}
	
	void composeJoin(QueryView view, QueryManifest manifest) {
		if(!isEmpty(joins)) {
			view.setJoins(unmodifiableCollection(joins));
			manifest.setRole(JOIN);
			manifest.prepareNested(view.getJoins());
		}
	}
	
	void composeUnion(QueryView view, QueryManifest manifest) {
		if(!isEmpty(unions)) {
			view.setUnions(unmodifiableCollection(unions)); 
			manifest.setRole(UNION);
			manifest.prepareNested(view.getUnions());
		}
	}

	void composeGroupBy(QueryView view, QueryManifest manifest, int aggr) {
		if(nonNull(groups)) {
			view.setGroups(unmodifiableCollection(groups));
		}
		else if(aggr == MEASURE && nonNull(manifest.getGroups())) {
			view.setGroups(unmodifiableCollection(manifest.getGroups()));
		}
	}

	void composeFrom(QueryView view, QueryManifest manifest) {
		if(nonNull(froms)) {
			view.setFroms(unmodifiableCollection(froms));
		}
		else if(nonNull(manifest.getFroms())) {
			view.setFroms(unmodifiableCollection(manifest.getFroms()));
		}
	}
	
	void composeCte(QueryView view, QueryManifest manifest) {
		if(!isEmpty(manifest.getCtes())) {
			view.setCtes(unmodifiableCollection(manifest.getCtes()));
		}
		if(!isEmpty(manifest.getOverViews())) {
			view.setOverView(unmodifiableMap(manifest.getOverViews()));
		}
	}
	
	@Override
	public String toString() {
		return compose(NO_STORE).toString();
	}

	@Deprecated
	public boolean isAggregation() {
		return false;
	}
}

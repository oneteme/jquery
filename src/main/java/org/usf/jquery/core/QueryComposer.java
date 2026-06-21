package org.usf.jquery.core;

import static java.lang.Math.max;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.addAll;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.QueryAnalyzer.Stage.COLUMN;
import static org.usf.jquery.core.QueryAnalyzer.Stage.CRITERIA;
import static org.usf.jquery.core.QueryAnalyzer.Stage.CTE;
import static org.usf.jquery.core.QueryAnalyzer.Stage.JOIN;
import static org.usf.jquery.core.QueryAnalyzer.Stage.ORDER;
import static org.usf.jquery.core.QueryAnalyzer.Stage.UNION;
import static org.usf.jquery.core.QueryPart.MEASURE;
import static org.usf.jquery.core.QueryPart.SCALAR;
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
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
public final class QueryComposer implements Composer<Query> {
	
	private final List<Column> columns = new ArrayList<>();
	private List<Query> ctes;
	private List<Criteria> criterias;
	private List<Order> orders;
	private List<Join> joins; 
	private List<Union> unions;
	private List<Column> groups; 
	private List<View> froms;
	private boolean distinct;
	private int limit  = -1;
	private int offset = -1;
	
	private final Map<View, Query> overView = new HashMap<>();

	public QueryComposer cte(@NonNull Query cte) {
		getCtes().add(cte);
		return this;
	}
	
	public QueryComposer ctes(@NonNull Query... ctes) {
		addAll(getCtes(), ctes);
		return this;
	}
	
	public QueryComposer ctes(@NonNull Collection<Query> ctes) {
		getCtes().addAll(ctes);
		return this;
	}
	
	private Collection<Query> getCtes(){
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
	
	public QueryComposer from(@NonNull View view) {
		getFroms().add(view);
		return this;
	}

	public QueryComposer froms(@NonNull View... views) {
		addAll(getFroms(), views);
		return this;
	}
	
	public QueryComposer froms(@NonNull Collection<View> views) {
		getFroms().addAll(views);
		return this;
	}
	
	private Collection<View> getFroms(){
		if(isNull(froms)) {
			froms = new ArrayList<>();
		}
		return froms;
	}
	
	public QueryComposer join(@NonNull Join join) {
		getJoins().add(join);
		return this;
	}
	
	public QueryComposer joins(@NonNull Join... joins) {
		addAll(getJoins(), joins);
		return this;
	}
	
	public QueryComposer joins(@NonNull Collection<Join> joins) {
		getJoins().addAll(joins);
		return this;
	}
	
	private Collection<Join> getJoins(){
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
	
	public QueryComposer union(@NonNull Union union) {
		addAll(getUnions(), union);
		return this;
	}
	
	public QueryComposer unions(@NonNull Union... unions) {
		addAll(getUnions(), unions);
		return this;
	}
	
	public QueryComposer unions(@NonNull Collection<Union> unions) {
		getUnions().addAll(unions);
		return this;
	}
	
	private Collection<Union> getUnions(){
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

	@Override
	public Query compose(Store store) {
		if(!isEmpty(columns)) {
			return compose(new Query(store));
		}
		throw new ComposeException("query requires at least one column");
	}
	
	Query compose(Query view) {
		log.trace("composing query..");
		var ct = currentTimeMillis();
		var nestCtes = new LinkedHashSet<Query>();
		var mnf = new QueryAnalyzer(view.getStore(), nestCtes, 
				isNull(froms) ? new LinkedHashSet<>() : null, 
				isNull(groups) ? new LinkedHashSet<>() : null,
				nonNull(joins) ? unmodifiableCollection(joins) : null, //read only
				new LinkedHashMap<>(overView));
		if(nonNull(this.ctes)) {
			mnf.setStage(CTE);
			mnf.analyzeNested(this.ctes);
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
	
	int composeColumn(Query view, QueryAnalyzer manifest) {
		if(!isEmpty(columns)) {
			view.setSelects(unmodifiableCollection(columns));
			manifest.setStage(COLUMN);
			return manifest.analyzeNested(view.getSelects());
		}
		return SCALAR;
	}
	
	int composeCriteria(Query view, QueryAnalyzer analyzer) {
		var isAgg = SCALAR;
		if(!isEmpty(criterias)) {
			analyzer.setStage(CRITERIA);
			List<Criteria> whr = null;
			List<Criteria> hvn = null;
			for(var crt : criterias) {
				var v = crt.prepare(analyzer);
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
	
	int composeOrder(Query view, QueryAnalyzer analyzer) {
		if(!isEmpty(orders)) {
			view.setOrders(unmodifiableCollection(orders));
			analyzer.setStage(ORDER);
			return analyzer.analyzeNested(view.getOrders());
		}
		return SCALAR;
	}
	
	void composeJoin(Query view, QueryAnalyzer analyzer) {
		if(!isEmpty(joins)) {
			view.setJoins(unmodifiableCollection(joins));
			analyzer.setStage(JOIN);
			analyzer.analyzeNested(view.getJoins());
		}
	}
	
	void composeUnion(Query view, QueryAnalyzer analyzer) {
		if(!isEmpty(unions)) {
			view.setUnions(unmodifiableCollection(unions)); 
			analyzer.setStage(UNION);
			analyzer.analyzeNested(view.getUnions());
		}
	}

	void composeGroupBy(Query view, QueryAnalyzer analyzer, int aggr) {
		if(nonNull(groups)) {
			view.setGroups(unmodifiableCollection(groups));
		}
		else if(aggr == MEASURE && !isEmpty(analyzer.getGroups())) {
			view.setGroups(unmodifiableCollection(analyzer.getGroups()));
		}
	}

	void composeFrom(Query view, QueryAnalyzer analyzer) {
		if(nonNull(froms)) {
			view.setFroms(unmodifiableCollection(froms));
		}
		else if(nonNull(analyzer.getFroms())) {
			view.setFroms(unmodifiableCollection(analyzer.getFroms()));
		}
	}
	
	void composeCte(Query view, QueryAnalyzer analyzer) {
		if(!isEmpty(analyzer.getCtes())) {
			view.setCtes(unmodifiableCollection(analyzer.getCtes()));
		}
		if(!isEmpty(analyzer.getOverViews())) {
			view.setOverView(unmodifiableMap(analyzer.getOverViews()));
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

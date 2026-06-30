package org.usf.jquery.core;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Stream.empty;
import static org.usf.jquery.core.QueryPart.DIMENSION;
import static org.usf.jquery.core.QueryPart.MEASURE;
import static org.usf.jquery.core.QueryPart.SCALAR;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author u$f
 *
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryAnalyzer {

	private final Store store; //TODO check usage
	private final Set<Query> ctes;
	private final Set<View> froms;  //optional, if null then no from allowed
	private final Set<Column> groups; //optional, if null then no group keys allowed
	private final Collection<Join> joins; //read only
	private final Map<View, Query> overViews;

	@Setter(AccessLevel.PACKAGE)
	private Stage stage;
	
	public static final int IGNORE_GROUPS = -1;
	public static final int ISOLATE_GROUPS = 1;
	
	QueryAnalyzer(Store store, Set<Query> ctes, Set<View> froms, Set<Column> groups, Collection<Join> joins, Map<View, Query> overViews) {
		this(store, ctes, froms, groups, joins, overViews, null);
	}

	public QueryAnalyzer cte(Query cte) {
		return cte(cte, false);
	}
	
	public QueryAnalyzer cte(Query cte, boolean overView) {
		ctes.add(cte);
		if(overView && !isEmpty(cte.getFroms())) {
			for(var s : cte.getFroms()) {
				overViews.compute(s, (k,v)-> {
					if(isNull(v) || v == cte) {
						return cte;
					}
					throw new IllegalStateException("conflict");
				});
			}
		}
		return this;
	}
	
	public QueryAnalyzer from(View view) {
		if(nonNull(froms) && (isNull(joins) || joins.stream()
				.noneMatch(j-> j.getView() == view || overViews.get(j.getView()) == view))) {
			froms.add(view);
		}
		return this;
	}

	public QueryAnalyzer groupBy(Column column) {
		if(nonNull(groups)) {
			groups.add(column);
		}
		return this;
	}
	
	public Set<View> getFroms() {
		if(Utils.isEmpty(overViews)) {
			return froms;
		}
		return froms.stream().map(v-> overViews.containsKey(v) ? overViews.get(v) : v).collect(Collectors.toSet());
	}
	
	public int analyzeNested(Collection<? extends QueryPart> arr){
		return analyzeNested(streamOrEmpty(arr), null);
	}
	
	public int analyzeNested(Collection<? extends QueryPart> arr, Column defaultDimension){
		return analyzeNested(streamOrEmpty(arr), defaultDimension);
	}
	
	public int tryAnalyzeNested(Object obj){
		return obj instanceof QueryPart o ? o.prepare(this) : SCALAR;
	}
		
	public int tryAnalyzeNested(Collection<?> arr){
		return analyzeNested(filterStream(arr), null);
	}
	
	public int tryAnalyzeNested(Collection<?> arr, int from){
		return analyzeNested(filterStream(arr).skip(from), null);
	}

	public int tryAnalyzeNested(Collection<?> arr, Column defaultDimension){
		return analyzeNested(filterStream(arr), defaultDimension);
	}

	int analyzeNested(Stream<? extends QueryPart> stream, Column defaultDimension){
		if(isNull(groups) || isNull(defaultDimension)) {
			return stream.mapToInt(o-> o.prepare(this)).max().orElse(SCALAR);
		}
		var sub = with(ISOLATE_GROUPS);
		var lvl = stream.mapToInt(o-> o.prepare(sub)).max().orElse(SCALAR);
		if(lvl == DIMENSION) { //group keys
			groups.add(defaultDimension);
		}
		else if(lvl == MEASURE && !sub.getGroups().isEmpty()) {
			sub.getGroups().forEach(groups::add);
		}
		return lvl;
	}
	
	public QueryAnalyzer with(int groupFlag) {
		var grp = switch (groupFlag) {
		case 0-> groups; //no changes
		case IGNORE_GROUPS-> null;
		case ISOLATE_GROUPS-> new LinkedHashSet<Column>();
		default-> throw new UnsupportedOperationException("flag="+groupFlag);
		};
		return new QueryAnalyzer(store, ctes, froms, grp, joins, overViews, stage);
	}
		
	private static <T> Stream<QueryPart> filterStream(Collection<T> arr) {
		return streamOrEmpty(arr).mapMulti((o, acc)->{
			if(o instanceof QueryPart n) {
				acc.accept(n);
			}
		});
	}

	private static <T> Stream<T> streamOrEmpty(Collection<T> arr) {
		return isEmpty(arr) ? empty() : arr.stream();
	}
	
	public enum Stage {
		CTE, COLUMN, CRITERIA, JOIN, ORDER, UNION;
	}
}

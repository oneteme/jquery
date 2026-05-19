package org.usf.jquery.core;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Stream.empty;
import static org.usf.jquery.core.DBObject.DIMENSION;
import static org.usf.jquery.core.DBObject.MEASURE;
import static org.usf.jquery.core.DBObject.SCALAR;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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
public final class QueryManifest {

	@Getter
	private final Store store; //TODO check usage
	private final Set<QueryView> ctes;
	private final Set<DBView> froms;  //nullable
	private final Set<Column> groups; //nullable
	private final Collection<ViewJoin> joins; //read only
	private final Map<DBView, QueryView> overViews;

	private boolean appendGroup;
	@Setter(AccessLevel.PACKAGE)
	private Section role;
	
	QueryManifest(Store store, Set<QueryView> ctes, Set<DBView> froms, Set<Column> groups, Collection<ViewJoin> joins, Map<DBView, QueryView> overViews) {
		this(store, ctes, froms, groups, joins, overViews, nonNull(groups), null);
	}

	public QueryManifest cte(QueryView cte) {
		return cte(cte, false);
	}
	
	public QueryManifest cte(QueryView cte, boolean overView) {
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
	
	public QueryManifest from(DBView view) {
		if(nonNull(froms) && (isNull(joins) || joins.stream()
				.noneMatch(j-> j.getView() == view || overViews.get(j.getView()) == view))) {
			froms.add(view);
		}
		return this;
	}

	public QueryManifest groupBy(Column column) {
		if(nonNull(groups) && appendGroup) {
			groups.add(column);
		}
		return this;
	}
	
	public int prepareNested(Collection<? extends DBObject> args){
		return prepareNestedOrElse(args, null);
	}
	
	public int prepareNestedOrElse(Collection<? extends DBObject> args, Column col){
		return prepareNested(streamOrEmpty(args), col);
	}

	public int tryPrepareNested(Collection<?> args){
		return tryPrepareNestedOrElse(args, null);
	}

	public int tryPrepareNestedOrElse(Collection<?> args, Column col){
		return prepareNested(streamOrEmpty(args).mapMulti((o, acc)->{
			if(o instanceof DBObject n) {
				acc.accept(n);
			}
		}), col);
	}

	int prepareNested(Stream<? extends DBObject> stream, Column elseGroupBy){
		if(isNull(groups) || !appendGroup) {
			stream.forEach(o-> o.prepare(this)); //declare views only, no group keys
			return SCALAR;
		}
		if(isNull(elseGroupBy)) {
			return stream.mapToInt(o-> o.prepare(this)).max().orElse(SCALAR);
		}
		var sub = new QueryManifest(store, ctes, froms, new LinkedHashSet<>(), joins, overViews, appendGroup, role);
		var lvl = stream.mapToInt(o-> o.prepare(sub)).max().orElse(SCALAR);
		if(lvl == DIMENSION) { //group keys
			groups.add(elseGroupBy);
		}
		else if(lvl == MEASURE && !sub.getGroups().isEmpty()) {
			sub.getGroups().forEach(groups::add);
		}
		return lvl;
	}
	
	public <T> T ignoreGroups(Function<QueryManifest, T> declaration) {
		if(nonNull(groups) && appendGroup) {
			try {
				appendGroup = false;
				return declaration.apply(this);
			}
			finally {
				appendGroup = true;
			}
		}
		return declaration.apply(this);
	}

	private static <T> Stream<T> streamOrEmpty(Collection<T> arr) {
		return isEmpty(arr) ? empty() : arr.stream();
	}
	
	public enum Section {
		CTE, COLUMN, CRITERIA, ORDER, JOIN, UNION;
	}

}

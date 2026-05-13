package org.usf.jquery.core;

import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNullElseGet;
import static java.util.stream.Stream.empty;
import static org.usf.jquery.core.DBObject.DIMENSION;
import static org.usf.jquery.core.DBObject.MEASURE;
import static org.usf.jquery.core.DBObject.SCALAR;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.LinkedHashMap;
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

	private final Store store; //?????
	private final Set<QueryView> ctes;
	private final Set<DBView> froms; //views
	private final Set<Column> groups;
	private final Map<DBView, QueryView> overViews;

	private boolean appendGroup;
	@Setter(AccessLevel.PACKAGE)
	private Section role;
	
	QueryManifest(Store store, Set<QueryView> ctes, Set<DBView> froms, Set<Column> groups, Map<DBView, QueryView> overViews) {
		this(store, ctes, froms, groups, requireNonNullElseGet(overViews, LinkedHashMap::new), nonNull(groups), null);
	}

	public QueryManifest cte(QueryView cte) {
		return cte(cte, false);
	}
	
	public QueryManifest cte(QueryView cte, boolean overView) {
		ctes.add(cte);
		if(overView && nonNull(cte.getFroms())) {
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
		if(nonNull(froms)) {
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
	
	public int prepareNested(DBObject... args){
		return prepareNestedOrElse(null, args);
	}
	
	public int prepareNestedOrElse(Column col, DBObject... args){
		return prepareNested(streamOrEmpty(args), col);
	}

	public int tryPrepareNested(Object... args){
		return tryPrepareNestedOrElse(null, args);
	}

	public int tryPrepareNestedOrElse(Column col, Object... args){
		return prepareNested(streamOrEmpty(args).mapMulti((o, acc)->{
			if(o instanceof DBObject n) {
				acc.accept(n);
			}
		}), col);
	}

	int prepareNested(Stream<DBObject> stream, Column elseGroupBy){
		if(isNull(groups) || !appendGroup) {
			stream.forEach(o-> o.prepare(this)); //declare views only, no group keys
			return SCALAR;
		}
		if(isNull(elseGroupBy)) {
			return stream.mapToInt(o-> o.prepare(this)).max().orElse(SCALAR);
		}
		var sub = new QueryManifest(store, ctes, froms, new LinkedHashSet<>(), overViews, appendGroup, role);
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

	private static <T> Stream<T> streamOrEmpty(T[] arr) {
		return isEmpty(arr) ? empty() : stream(arr);
	}
	
	static <T> Set<T> resolveSet(Set<T> set) {
		return nonNull(set) ? unmodifiableSet(set) : new LinkedHashSet<>();
	}
	
	public enum Section {
		
		COLUMN,
		CRITERIA,
		ORDER;
	}

}

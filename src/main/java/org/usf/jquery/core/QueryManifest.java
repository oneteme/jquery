package org.usf.jquery.core;

import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNullElseGet;
import static java.util.stream.Stream.empty;
import static org.usf.jquery.core.DBObject.SCALAR;
import static org.usf.jquery.core.DBObject.DIMENSION;
import static org.usf.jquery.core.DBObject.MEASURE;
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
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class QueryManifest {

	private final Set<QueryView> ctes;
	private final Set<DBView> froms; //views
	private final Set<Column> groups;
	private final Map<DBView, QueryView> overViews;
	
	private boolean scanFroms;
	private boolean scanGroups;
	@Setter(AccessLevel.PACKAGE)
	private Section role;
	
	QueryManifest(Set<QueryView> ctes, Set<DBView> froms, Set<Column> groups, Map<DBView, QueryView> overViews) {
		this(secureSet(ctes), 
				secureSet(froms), 
				secureSet(groups), 
				requireNonNullElseGet(overViews, LinkedHashMap::new), isNull(froms), isNull(groups), null);
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
		if(scanFroms) {
			froms.add(view);
		}
		return this;
	}

	public QueryManifest groupBy(Column column) {
		if(scanGroups) {
			groups.add(column);
		}
		return this;
	}
	
	public int prepareNested(DBObject... args){
		return prepareNestedOrElse(args, null);
	}
	
	public int prepareNestedOrElse(DBObject[] args, Column col){
		return prepareNested(streamOrEmpty(args), col);
	}

	public int tryPrepareNested(Object... args){
		return tryPrepareNestedOrElse(args, null);
	}

	public int tryPrepareNestedOrElse(Object[] args, Column col){
		return prepareNested(streamOrEmpty(args).mapMulti((o, acc)->{
			if(o instanceof DBObject n) {
				acc.accept(n);
			}
		}), col);
	}

	int prepareNested(Stream<DBObject> stream, Column elseGroupBy){
		if(!scanGroups) {
			stream.forEach(o-> o.prepare(this)); //declare views only, no group keys
			return SCALAR;
		}
		if(isNull(elseGroupBy)) {
			return stream.mapToInt(o-> o.prepare(this)).max().orElse(SCALAR);
		}
		var sub = new QueryManifest(ctes, froms, new LinkedHashSet<>(), overViews, scanFroms, scanGroups, role);
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
		if(scanGroups) {
			try {
				scanGroups = false;
				return declaration.apply(this);
			}
			finally {
				scanGroups = true;
			}
		}
		return declaration.apply(this);
	}

	private static <T> Stream<T> streamOrEmpty(T[] arr) {
		return isEmpty(arr) ? empty() : stream(arr);
	}
	
	static <T> Set<T> secureSet(Set<T> set) {
		return nonNull(set) ? unmodifiableSet(set) : new LinkedHashSet<>();
	}
	
	public enum Section {
		
		COLUMN,
		CRITERIA,
		ORDER;
	}

}

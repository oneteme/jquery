package org.usf.jquery.core;

import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * 
 * @author u$f
 *
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class QueryView implements DBView, Nested {

	private final QueryComposer composer;
	
	@Override
	public void build(QueryBuilder query) {
		var sub = query.subQuery(composer.getViews());
		sub.appendParenthesis(()-> composer.build(sub));
	}

	@Override
	public int compose(QueryComposer query, Consumer<DBColumn> groupKeys) {
		var ctes = composer.getCtes();
		if(!ctes.isEmpty()) {
			query.ctes(ctes.toArray(QueryView[]::new));  //cte propagation
		}
		return -1; //no column
	}
	
	public SingleColumnQuery asColumn(){
		return new SingleColumnQuery(this);
	}
	
	public QueryUnion asUnion(boolean all) {
		return new QueryUnion(all, this);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this); 
	}
}
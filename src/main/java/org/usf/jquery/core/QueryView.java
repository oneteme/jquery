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
@Setter(AccessLevel.PACKAGE)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class QueryView implements DBView, Nested {

	private final QueryComposer composer;
	
	@Override
	public void build(QueryBuilder query) {
		var sub = query.subQuery(composer.getViews());
		query.appendParenthesis(()-> composer.build(sub));
	}

	@Override
	public int compose(QueryComposer query, Consumer<DBColumn> groupKeys) {
		var ctes = composer.getCtes();
		if(!ctes.isEmpty()) {
			query.ctes(ctes.toArray(QueryView[]::new));  //up
		}
		return -1; //no column
	}
	
	public SingleColumnQuery asColumn(){
		return new SingleColumnQuery(this);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this); 
	}
}
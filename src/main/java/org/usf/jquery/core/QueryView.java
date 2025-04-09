package org.usf.jquery.core;

import static java.util.Objects.nonNull;

import java.util.function.BiConsumer;
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
@Setter(AccessLevel.PACKAGE)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class QueryView implements DBView, Nested {

	@Getter
	private final QueryComposer composer;
	private BiConsumer<QueryBuilder, QueryBuilder> callback; //cte
	
	@Override
	public void sql(QueryBuilder query) {
		var sub = query.subQuery(composer.getViews());
		query.parenthesis(()-> composer.build(sub));
		if(nonNull(callback)) { 
			callback.accept(query, sub);
		}
	}

	@Override
	public int compose(QueryComposer query, Consumer<DBColumn> groupKeys) {
		var ctes = this.composer.getCtes();
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
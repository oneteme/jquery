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
	private final RequestComposer composer;
	private BiConsumer<QueryContext, QueryContext> callback; //cte
	
	@Override
	public void sql(SqlStringBuilder sb, QueryContext ctx) {
		var sub = ctx.subQuery(composer.getViews());
		sb.parenthesis(()-> composer.build(sb, sub));
		if(nonNull(callback)) { 
			callback.accept(ctx, sub);
		}
	}

	@Override
	public int declare(RequestComposer composer, Consumer<DBColumn> groupKeys) {
		if(!this.composer.getCtes().isEmpty()) { //up
			composer.ctes(this.composer.getCtes().toArray(QueryView[]::new));
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
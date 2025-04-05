package org.usf.jquery.core;

import static java.util.Objects.nonNull;

import java.util.function.BiConsumer;

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
public final class QueryView implements DBView {

	@Getter
	private final RequestComposer composer;
	private BiConsumer<QueryContext, QueryContext> callback;
	
	@Override
	public void sql(SqlStringBuilder sb, QueryContext ctx) {
		var sub = ctx.subQuery(composer.getCtes(), composer.getViews());
		sb.parenthesis(()-> composer.build(sb, sub));
		if(nonNull(callback)) { 
			callback.accept(ctx, sub);
		}
	}
	
	public SingleColumnQuery asColumn(){
		return new SingleColumnQuery(this);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this); 
	}
}
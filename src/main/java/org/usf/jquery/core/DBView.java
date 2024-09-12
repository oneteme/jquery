package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNoArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface DBView extends DBObject {

	void sql(SqlStringBuilder sb, QueryContext ctx);
	
	@Override
	default void sql(SqlStringBuilder sb, QueryContext ctx, Object[] args) {
		requireNoArgs(args, DBView.class::getSimpleName);
		sql(sb, ctx);
	}
	
	default void sqlWithTag(SqlStringBuilder sb, QueryContext ctx) {
		ctx.viewOverload(this).orElse(this).sql(sb, ctx); //!important
		sb.space().append(ctx.viewAlias(this));
	}
}

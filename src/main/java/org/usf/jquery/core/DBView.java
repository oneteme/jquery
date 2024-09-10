package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Validation.requireNoArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface DBView extends DBObject {

	String sql(QueryContext ctx);
	
	@Override
	default String sql(QueryContext ctx, Object[] args) {
		requireNoArgs(args, DBView.class::getSimpleName);
		return sql(ctx);
	}
	
	default String sqlWithTag(QueryContext ctx) {
		return ctx.viewOverload(this).orElse(this).sql(ctx) //!important
				+ SPACE + ctx.viewAlias(this);
	}
}

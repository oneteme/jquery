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
	
	default void sqlUsingTag(SqlStringBuilder sb, QueryContext ctx) {
		sql(sb, ctx);
		sb.appendSpace().append(ctx.viewAlias(this));
	}

	default ViewColumn column(String name) {
		return new ViewColumn(name, this, null, null);
	}

	default ViewColumn column(String name, JDBCType type) {
		return new ViewColumn(name, this, type, null);
	}
	
	default ViewColumn column(String name, JDBCType type, String tag) {
		return new ViewColumn(name, this, type, tag);
	}
}

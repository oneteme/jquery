package org.usf.jquery.core;

import java.util.function.Consumer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@Getter
@RequiredArgsConstructor
public class ViewColumn implements NamedColumn {

	private final String name;
	private final DBView view; //optional
	private final JDBCType type; //optional
	private final String tag;  //optional
	
	@Override
	public void sql(SqlStringBuilder sb, QueryContext ctx) {
		sb.appendIfNonNull(view, v-> ctx.viewAlias(v) + '.').append(name);
	}
	
	@Override
	public boolean resolve(QueryBuilder builder, Consumer<? super DBColumn> groupKeys) {
		return false;
	}

	@Override
	public void views(Consumer<DBView> cons) {
		cons.accept(view);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}

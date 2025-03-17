package org.usf.jquery.core;

import static java.util.Objects.nonNull;

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
		if(nonNull(view)) {
			sb.append(ctx.viewAlias(view) + '.');
		}
		sb.append(name);
	}
	
	@Override
	public int columns(QueryBuilder builder, Consumer<DBColumn> groupKeys) {
		groupKeys.accept(this);
		return 0;
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

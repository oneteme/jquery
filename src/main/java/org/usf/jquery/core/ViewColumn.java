package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.QueryContext.addWithValue;
import static org.usf.jquery.core.SqlStringBuilder.member;

import java.util.Collection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@Getter
@RequiredArgsConstructor
public final class ViewColumn implements NamedColumn {

	private final String name;
	private final DBView view; //optional
	private final JDBCType type; //optional
	private final String tag;  //optional
	
	@Override
	public String sql(QueryContext ctx) {
		return nonNull(view) ? member(ctx.viewAlias(view), name) : name;
	}
	
	public boolean resolve(QueryBuilder builder) {
		return false;
	}
	
	public void views(Collection<DBView> views) {
		views.add(view);
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}
}

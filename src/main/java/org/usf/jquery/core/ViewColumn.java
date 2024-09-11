package org.usf.jquery.core;

import static java.util.Objects.nonNull;

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
public class ViewColumn implements NamedColumn {

	private final String name;
	private final DBView view; //optional
	private final JDBCType type; //optional
	private final String tag;  //optional
	
	@Override
	public void sql(SqlStringBuilder sb, QueryContext ctx) {
		sb.appendIf(nonNull(view), ()-> ctx.viewAlias(view) + '.').append(name);
	}
	
	public boolean resolve(QueryBuilder builder) {
		return false;
	}
	
	public void views(Collection<DBView> views) {
		if(nonNull(view)) {
			views.add(view);
		}
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}

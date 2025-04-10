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
	public void build(QueryBuilder query) {
		(nonNull(view) ? query.appendViewAlias(view).append(".") : query)
		.append(name);
	}
	
	@Override
	public int compose(QueryComposer query, Consumer<DBColumn> groupKeys) {
		if(nonNull(view)) {
			query.declare(view);
		}
		groupKeys.accept(this);
		return 0;
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}

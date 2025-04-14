package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.SqlStringBuilder.DOT;

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
	public int compose(QueryComposer query, Consumer<DBColumn> groupKeys) {
		if(nonNull(view)) {
			query.declare(view);
		}
		if(nonNull(groupKeys)) { //unsafe
			groupKeys.accept(this);
		}
		return 0;
	}
	
	@Override
	public void build(QueryBuilder query) {
		(nonNull(view) ? query.appendViewAlias(view, DOT) : query)
		.append(name);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}

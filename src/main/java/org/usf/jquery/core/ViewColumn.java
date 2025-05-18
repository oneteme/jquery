package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.SqlStringBuilder.DOT;

import java.util.function.Consumer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;

/**
 * 
 * @author u$f
 *
 */
@Getter
@RequiredArgsConstructor
public class ViewColumn implements NamedColumn{

	private final String name;
	private final DBView view; //optional
	private final JDBCType type; //optional
	private final String tag;  //optional
	@With
	private final Adjuster<String> adjuster; //column name adjuster

	public ViewColumn(String name, DBView view, JDBCType type, String tag) {
		this(name, view, type, tag, null);
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
	public void build(QueryBuilder query) {
		if(nonNull(view)) {
			query.appendViewAlias(view, DOT);
		}
		query.append(nonNull(adjuster) ? adjuster.build(query, name) : name);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}

package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.SqlStringBuilder.DOT;

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
	public int prepare(QueryManifest query) {
		if(nonNull(view)) {
			query.from(view);
		}
		query.groupBy(this);
		return DIMENSION;
	}
	
	@Override
	public void build(QueryBuilder query) {
		if(nonNull(view)) {
			query.appendViewAlias(view, DOT);
		}
		query.append(name);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}

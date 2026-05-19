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
public class ViewColumn implements Column {

	private final String name;
	private final DBView view; //optional
	private final JDBCType type; //optional
	private final String tag;  //optional

	@Override
	public int prepare(QueryManifest manifest) {
		if(nonNull(view)) {
			manifest.from(view);
		}
		manifest.groupBy(this);
		return DIMENSION;
	}
	
	@Override
	public void build(QueryBuilder builder) {
		if(nonNull(view)) {
			builder.appendViewAlias(view, DOT);
		}
		builder.append(name);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}

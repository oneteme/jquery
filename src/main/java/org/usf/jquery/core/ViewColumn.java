package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.SqlBuilder.DOT;

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
	private final View view; //optional
	private final JDBCType type; //optional
	private final String tag;  //optional

	@Override
	public int prepare(QueryAnalyzer analyzer) {
		if(nonNull(view)) {
			analyzer.from(view);
		}
		analyzer.groupBy(this);
		return DIMENSION;
	}
	
	@Override
	public void build(SqlBuilder builder) {
		if(nonNull(view)) {
			builder.appendViewAlias(view, DOT);
		}
		builder.append(name);
	}
	
	@Override
	public String toString() {
		return QueryPart.toSQL(this);
	}
}

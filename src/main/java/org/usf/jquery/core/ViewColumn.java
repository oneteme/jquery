package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.QueryVariables.addWithValue;
import static org.usf.jquery.core.SqlStringBuilder.member;

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
	public String sql(QueryVariables qv) {
		return nonNull(view) ? member(qv.viewAlias(view), name) : name;
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}
}

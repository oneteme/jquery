package org.usf.jquery.core;

import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;
import static org.usf.jquery.core.SqlStringBuilder.parenthese;

import java.util.Collection;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class QueryView implements DBQuery {

	private final String id;
	@Getter // remove this
	private final RequestQueryBuilder builder;
	
	@Override
	public String sql(QueryParameterBuilder param) {
		var s = new SqlStringBuilder(100);
		builder.build(s, param.subQuery()); //important! build query first
		return parenthese(s.toString());
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public Collection<TaggableColumn> columns() {
		return builder.getColumns();
	}

	@Override
	public String toString() {
		return sql(addWithValue()); 
	}	
}

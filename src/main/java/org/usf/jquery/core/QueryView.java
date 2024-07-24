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
	private final RequestQueryBuilder query;
	
	public QueryView(String id, TaggableColumn... columns) {
		this(id, new RequestQueryBuilder().columns(columns));
	}

	@Override
	public String sql(QueryParameterBuilder builder) {
		var s = new SqlStringBuilder(100);
		query.build(s, builder.subQuery()); //important! build query first
		return parenthese(s.toString());
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public Collection<TaggableColumn> columns() {
		return query.getColumns();
	}
	
	@Override
	public String toString() {
		return sql(addWithValue()); 
	}
}

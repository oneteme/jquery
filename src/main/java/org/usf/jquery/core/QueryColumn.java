package org.usf.jquery.core;

import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;
import static org.usf.jquery.core.Validation.requireNArgs;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class QueryColumn implements DBColumn {
	
	private final QueryView query;
	@Getter
	private final JDBCType type;

	@Override
	public String sql(QueryParameterBuilder builder) {
		requireNArgs(1, query.getBuilder().getColumns().toArray(), ()-> ""); //TODO
		return query.sql(builder);
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}
}

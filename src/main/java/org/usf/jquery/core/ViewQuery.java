package org.usf.jquery.core;

import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ViewQuery implements DBView {

	private final String id;
	private final RequestQueryBuilder query;
	
	public ViewQuery(@NonNull String id, @NonNull TaggableColumn... columns) {
		this(id, new RequestQueryBuilder().columns(columns));
	}

	@Override
	public String sql(QueryParameterBuilder builder) {
		var s = new SqlStringBuilder(100).append("(");
		query.build(s, builder.subQuery());
		return s.append(")").toString();
	}

	@Override
	public String id() {
		return id;
	}
	
	public ViewQuery columns(TaggableColumn... columns) {
		query.columns(columns);
		return this;
	}

	public ViewQuery filters(DBFilter... filters){
		query.filters(filters);
		return this;
	}
	
	@Override
	public String toString() {
		return sql(addWithValue()); 
	}
}

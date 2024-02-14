package org.usf.jquery.core;

import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;

import org.usf.jquery.core.JavaType.Typed;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class ViewQuery implements DBView, Typed {

	private final String id;
	@Getter
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

	@Override
	public JavaType getType() {
		if(query.getColumns().size() == 1) {
			return query.getColumns().get(0).getType();
		}
		var msg = query.getColumns().isEmpty() ? "no columns" : "too many columns";
		throw new UnsupportedOperationException(msg + " : " + this);
	}
	
	@Override
	public String toString() {
		return sql(addWithValue()); 
	}
}

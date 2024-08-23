package org.usf.jquery.core;

import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class QueryView implements DBView {

	@Getter // remove this
	private final RequestQueryBuilder builder;
	
	@Override
	public String sql(QueryParameterBuilder param) {
		var s = new SqlStringBuilder(100).append("(");
		builder.build(s, param.subQuery());
		return s.append(")").toString();
	}
	
	public QueryColumn asColumn(){
		if(builder.getColumns().size() == 1) {
			return new QueryColumn(this, builder.getColumns().get(0).getType());
		}
		throw new IllegalStateException("too many column");
	}
	
	@Override
	public String toString() {
		return sql(addWithValue()); 
	}	
}

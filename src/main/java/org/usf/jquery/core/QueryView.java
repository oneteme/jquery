package org.usf.jquery.core;

import static org.usf.jquery.core.QueryVariables.addWithValue;

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

	@Getter
	private final QueryBuilder builder;
	
	@Override
	public String sql(QueryVariables param) {
		var s = new SqlStringBuilder(100).append("(");
		builder.build(s, param.subQuery(builder.getOverView()));
		return s.append(")").toString();
	}
	
	public SingleColumnQuery asColumn(){
		return new SingleColumnQuery(this);
	}
	
	@Override
	public String toString() {
		return sql(addWithValue()); 
	}	
}

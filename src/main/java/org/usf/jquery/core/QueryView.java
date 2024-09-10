package org.usf.jquery.core;

import static org.usf.jquery.core.QueryContext.addWithValue;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class QueryView implements DBView {

	private final QueryBuilder builder;
	
	@Override
	public String sql(QueryContext ctx) {
		var s = new SqlStringBuilder(100).append("(");
		builder.build(s, ctx.subQuery(builder.getOverView()));
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

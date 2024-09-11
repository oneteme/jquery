package org.usf.jquery.core;

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
	public void sql(SqlStringBuilder sb, QueryContext ctx) {
		sb.openParenthesis();
		builder.build(sb, ctx.subQuery(builder.getOverView()));
		sb.closeParenthesis();
	}
	
	public SingleColumnQuery asColumn(){
		return new SingleColumnQuery(this);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this); 
	}	
}

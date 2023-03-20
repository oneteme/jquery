package org.usf.jquery.core;

import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;

import java.util.LinkedList;
import java.util.List;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class CaseExpressionColumn implements DBColumn {

	private final List<WhenExpression> filters = new LinkedList<>();
	
	@Override
	public String sql(QueryParameterBuilder ph) {
		return new SqlStringBuilder(filters.size() * 50)
				.append("CASE ")
				.appendEach(filters, SPACE, f-> f.sql(ph, null))
				.append(" END").toString();
	}
		
	public CaseExpressionColumn append(WhenExpression ce) {
		filters.add(ce);
		return this;
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}

	public static CaseExpressionColumn caseWhen(DBFilter filter, Object value){
		return new CaseExpressionColumn()
				.append(new WhenExpression(filter, value));
	}
}

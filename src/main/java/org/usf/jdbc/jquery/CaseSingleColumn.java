package org.usf.jdbc.jquery;

import static org.usf.jdbc.jquery.QueryParameterBuilder.addWithValue;
import static org.usf.jdbc.jquery.SqlStringBuilder.SPACE_SEPARATOR;

import java.util.LinkedList;
import java.util.List;

public final class CaseSingleColumn implements DBColumn {

	private final List<CaseExpression> filters;
	
	CaseSingleColumn() {
		filters = new LinkedList<>();
	}
	
	@Override
	public String sql(QueryParameterBuilder ph) {
		//force static values
		return new SqlStringBuilder(filters.size() * 50)
				.append("CASE ")
				.appendEach(filters, SPACE_SEPARATOR, f-> f.sql(ph, null))
				.append(" END").toString();
	}
		
	void append(CaseExpression ce) {
		filters.add(ce);
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}

	public static CaseSingleColumn caseWhen(DBFilter filter, Object value){
		
		var cc = new CaseSingleColumn();
		cc.append(new CaseExpression(filter, value));
		return cc;
	}
}

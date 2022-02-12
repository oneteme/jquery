package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.DBTable.mockTable;
import static fr.enedis.teme.jquery.QueryParameterBuilder.addWithValue;
import static fr.enedis.teme.jquery.SqlStringBuilder.SPACE_SEPARATOR;

import java.util.LinkedList;
import java.util.List;

public final class CaseSingleColumn implements DBColumn {

	private final List<WhenExpression> filters;
	
	CaseSingleColumn() {
		filters = new LinkedList<>();
	}
	
	@Override
	public String sql(DBTable table, QueryParameterBuilder ph) {
		//force static values
		return ph.staticMode(()-> new SqlStringBuilder(filters.size() * 50)
				.append("CASE ")
				.appendEach(filters, SPACE_SEPARATOR, f-> f.sql(table, ph))
				.append(" END").toString());
	}
	
	@Override
	public boolean isExpression() {
		return true;
	}
	
	@Override
	public boolean isAggregation() {
		return false;
	}
	
	@Override
	public boolean isConstant() {
		return false;
	}
	
	void append(WhenExpression when) {
		filters.add(when);
	}
	
	@Override
	public String toString() {
		return sql(mockTable(), addWithValue());
	}
	
}

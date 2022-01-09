package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.DBTable.mockTable;
import static fr.enedis.teme.jquery.ParameterHolder.addWithValue;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Stream;

import lombok.NonNull;

public final class CaseColumn implements DBColumn {

	private final List<WhenExpression> filters;

	public CaseColumn(@NonNull WhenExpression... filters) {
		this.filters = Stream.of(filters).collect(toList());
	}
	
	@Override
	public String sql(DBTable table, ParameterHolder ph) {
		//force static values
		return ph.staticMode(()-> new SqlStringBuilder(filters.size() * 50)
				.append("CASE ")
				.append(filters.stream()
					.map(f-> f.sql(table, ph))
					.collect(joining(" "))) //optimize SQL 
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
	
	@Override
	public String toString() {
		return sql(mockTable(), addWithValue());
	}
	
}

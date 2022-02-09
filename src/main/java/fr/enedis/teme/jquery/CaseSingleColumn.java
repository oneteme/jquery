package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.DBTable.mockTable;
import static fr.enedis.teme.jquery.QueryParameterBuilder.addWithValue;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import lombok.NonNull;

public final class CaseSingleColumn implements DBColumn {

	private final List<WhenExpression> filters;
	
	CaseSingleColumn() {
		filters = new LinkedList<>();
	}

	public CaseSingleColumn(@NonNull WhenExpression... filters) {
		this.filters = Stream.of(filters).collect(toList());
	}
	
	@Override
	public String sql(DBTable table, QueryParameterBuilder ph) {
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
	
	void append(WhenExpression when) {
		filters.add(when);
	}
	
	@Override
	public String toString() {
		return sql(mockTable(), addWithValue());
	}
	
}

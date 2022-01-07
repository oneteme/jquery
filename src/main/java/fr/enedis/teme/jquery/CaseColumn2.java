package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.DBTable.mockTable;
import static fr.enedis.teme.jquery.ParameterHolder.addWithValue;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;

public final class CaseColumn2 implements DBColumn {

	private final String tagName;
	private final List<WhenCase> filters;

	public CaseColumn2(String tagName, WhenCase... filters) {
		super();
		this.tagName = tagName;
		this.filters = Stream.of(filters).collect(toList());
	}
	
	@Override
	public String sql(DBTable table, ParameterHolder ph) {
		return ph.staticMode(()-> new SqlStringBuilder(filters.size() * 50) //force static values
				.append("CASE ")
				.append(filters.stream()
					.map(f-> f.sql(table, ph))
					.collect(joining(" "))) //optimize SQL 
				.append(" END").toString());
	}

	@Override
	public String getTag() {
		return tagName;
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

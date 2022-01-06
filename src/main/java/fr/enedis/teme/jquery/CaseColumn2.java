package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.DBTable.mockTable;
import static fr.enedis.teme.jquery.ParameterHolder.addWithValue;
import static java.util.stream.Collectors.joining;

import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class CaseColumn2 implements DBColumn {

	private final WhenCase[] filters;
	private final String tagName;
	
	@Override
	public String sql(DBTable table, ParameterHolder ph) {
		return ph.staticMode(()-> new SqlStringBuilder(filters.length * 50) //force static values
				.append("CASE ")
				.append(Stream.of(filters)
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
	
	public static CaseColumn2 cases(WhenCase... filters){
		
		return new CaseColumn2(filters, null);
	}
}

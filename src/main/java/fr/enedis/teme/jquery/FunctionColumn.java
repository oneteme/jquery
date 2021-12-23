package fr.enedis.teme.jquery;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElseGet;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class FunctionColumn implements DBColumn {

	@NonNull
	private final DBExpression function;
	@NonNull
	private final DBColumn column;
	private final String mappedName; // nullable

	@Override
	public String getMappedName() {
		return requireNonNullElseGet(mappedName, ()-> function.mappedName(column.getMappedName()));
	}
	
	@Override
	public String getAlias(DBTable table) {
		return function.mappedName(column.toSql(requireNonNull(table)));
	}
	
	@Override
	public String toSql(DBTable table) {
		return function.toSql(column.toSql(requireNonNull(table)));
	}
	
	@Override
	public boolean isAggregated() {
		return function.isAggregation();
	}
	
	@Override
	public String toString() {
		return function.toSql(getMappedName());
	}
	
}

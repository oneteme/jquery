package fr.enedis.teme.jquery;

import static java.util.Objects.requireNonNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public abstract class CaseExpressionColumn implements TableColumn {

	private final DBColumn column;

	@Override
	public final String groupAlias(DBTable table) {
		return "case_" + requireNonNull(table).getColumnName(column);
	}
	
	@Override
	public final String getMappedName() {
		return column.getMappedName();
	}
	
	@Override
	public final String toSql(DBTable table) {
		
		return "CASE " + toSql(column.toSql(table)) + " END AS " + groupAlias(table);
	}
	
	protected abstract String toSql(String columnName);
	
	protected static String whenThen(String expression, String tag) {
		
		return "WHEN " + expression + " THEN '"+ tag+"'";
	}
}

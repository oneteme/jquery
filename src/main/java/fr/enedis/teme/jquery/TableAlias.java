package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.QueryParameterBuilder.addWithValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
final class TableAlias implements DBTable {
	
	private final DBTable table;
	private final String alias;

	@Override
	public String physicalName() {
		return table.physicalName();
	}

	@Override
	public String physicalColumnName(TableColumn column) {
		return alias + "." + table.physicalColumnName(column);
	}

	@Override
	public String logicalColumnName(TaggableColumn column) {
		return alias + "." + column.tagname();
	}

	@Override
	public TableColumn[] columns() {
		return table.columns();
	}

	@Override
	public String sql(String schema, String suffix, QueryParameterBuilder ph) {
		return DBTable.super.sql(schema, suffix, ph) + " " + alias;
	}
	
	public TableAlias map(String alias) {
		return new TableAlias(table, alias);
	}
	
	@Override
	public String toString() {
		return sql(null, addWithValue());
	}
}

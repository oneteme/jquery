package fr.enedis.teme.jquery;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class TableAlias implements DBTable {
	
	private final DBTable table;
	private final String alias;

	@Override
	public String physicalName() {
		return table.physicalName() + " " + alias;
	}

	@Override
	public String physicalColumnName(TableColumn column) {
		return alias + "." + table.physicalColumnName(column);
	}

	@Override
	public TableColumn[] columns() {
		return table.columns();
	}

}

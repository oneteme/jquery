package fr.enedis.teme.jquery;

import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
final class GenericTable implements DBTable {
	
	private final String tableName;
	private final Map<DBColumn, String> columnMap;
	private final DBColumn revisionColumn;
	
	@Override
	public String getColumnName(DBColumn column) {
		return columnMap.get(column);
	}

}

package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.GenericColumn.c1;
import static fr.enedis.teme.jquery.GenericColumn.c2;
import static fr.enedis.teme.jquery.GenericColumn.c3;
import static fr.enedis.teme.jquery.GenericColumn.c4;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableMap;

import java.util.Collection;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
final class GenericTable implements DBTable {
	
	static final String c1_name = "some_id";
	static final String c2_name = "some_label";
	static final String c3_name = "some_decription";
	static final String c4_name = "some_revision";
	
	static final DBTable tab1 = new GenericTable("someTable", 
			unmodifiableMap(Map.of(c1, c1_name, c2, c2_name, c3, c3_name, c4, c4_name)), c4);
	
	private final String tableName;
	private final Map<DBColumn, String> columnMap;
	private final DBColumn revisionColumn;


	@Override
	public String physicalName() {
		return tableName;
	}

	@Override
	public String physicalColumnName(TableColumn column) {
		return columnMap.get(column);
	}
	
	 @Override
	public TableColumn[] columns() {
		return columnMap.keySet().toArray(TableColumn[]::new);
	}

	@Override
	public Collection<ColumnTemplate> columnTemplates() {
		return emptyList();
	}
}

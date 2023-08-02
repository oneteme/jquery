package org.usf.jquery.web;

import static java.lang.String.join;
import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter(AccessLevel.PACKAGE)
@ToString
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class TableMetadata {
	
	private final String tablename;
	private final Map<String, ColumnMetadata> columns;
	
	void fetch(DatabaseMetaData metadata) throws SQLException {
		var dbMap = columns.values().stream().collect(toMap(ColumnMetadata::getColumnName, identity()));
		try(var rs = metadata.getColumns(null, null, tablename, null)){
			if(!rs.next()) {
				throw new NoSuchElementException(tablename + " table not found");
			}
			do {
				var cn = rs.getString("COLUMN_NAME");
				if(dbMap.containsKey(cn)) {
					var meta = dbMap.remove(cn);
					meta.setDataSize(rs.getInt("COLUMN_SIZE"));
					meta.setDataType(rs.getInt("DATA_TYPE"));
				}// else undeclared column
			} while(rs.next());
		}
		if(!dbMap.isEmpty()) {
			throw new NoSuchElementException("column(s) [" + join(", ", dbMap.keySet()) + "] not found in " + tablename);
		}
	}
	
	public static TableMetadata yearTableMetadata(TableDecorator table, Collection<ColumnDecorator> columns) {
		var map = new LinkedHashMap<String, ColumnMetadata>();
		columns.stream().forEach(cd-> 
			table.columnName(cd).ifPresent(cn-> 
				map.put(cd.identity(), new ColumnMetadata(cn))));
		return new TableMetadata(table.tableName(), unmodifiableMap(map));
	}

}

package org.usf.jquery.web;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.Utils.AUTO_TYPE;
import static org.usf.jquery.core.Utils.UNLIMITED;
import static org.usf.jquery.web.DatabaseScanner.database;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@Getter(value = AccessLevel.PACKAGE)
@RequiredArgsConstructor
public class TableDecoratorWrapper implements TableDecorator {

	@Delegate
	private final TableDecorator table;
	private final Map<String, ColumnMetadata> columns = new LinkedHashMap<>();

	@Override
	public int columnType(ColumnDecorator desc) {
		var type = table.columnType(desc); //overridden
		if(type == AUTO_TYPE && columns.containsKey(desc.identity())) {
			type = columns.get(desc.identity()).getDataType();
		}
		return type;
	}

	@Override
	public int columnSize(ColumnDecorator desc) {
		var size = table.columnSize(desc); //overridden
		if(size == UNLIMITED && columns.containsKey(desc.identity())) {
			size = columns.get(desc.identity()).getDataSize();
		}
		return size;
	}

	void fetch(DatabaseMetaData metadata) throws SQLException {
		columns.clear();
		var declaredCols = declaredColumn();
		try(var rs = metadata.getColumns(null, null, tableName(), null)){
			if(!rs.next()) {
				throw new NoSuchElementException(tableName() + " table not found");
			}
			do {
				var cn = rs.getString("COLUMN_NAME");
				var dc = declaredCols.get(cn);
				if(nonNull(dc)) {
					columns.put(dc.identity(), new ColumnMetadata(cn,  
							rs.getInt("DATA_TYPE"), 
							rs.getInt("COLUMN_SIZE")));
				}
				//else undeclared column
			} while(rs.next());
		}
		checkExistingColumns(tableName(), declaredCols);
		logTableColumns(getColumns());
	}
	
	void checkExistingColumns(String tn, Map<String, ColumnDecoratorWrapper> map) {
		if(columns.size() < map.size()) {
			throw new NoSuchElementException(map.values().stream()
					.filter(c-> !columns.containsKey(c.identity()))
					.map(ColumnDecoratorWrapper::unwrap)
					.map(this::columnName)
					.collect(joining(",", "[", "]")) + " column(s) not found in " + tn);
		}
	}
	
	/**
	 * key = columnName
	 */
	Map<String, ColumnDecoratorWrapper> declaredColumn() {
		var dc = new LinkedHashMap<String, ColumnDecoratorWrapper>();
		database().columns().forEach(cd-> {
			var cn = columnName(cd.unwrap());
			if(nonNull(cn)) { //should not throw exception
				dc.put(cn, cd);
			}
		});
		return dc;
	}
	
	
	static void logTableColumns(Map<String, ColumnMetadata> map) {
		if(!map.isEmpty()) {
			var pattern = "|%-20s|%-40s|%-6s|%-12s|";
			var bar = format(pattern, "", "", "", "").replace("|", "+").replace(" ", "-");
			log.info(bar);
			log.info(format(pattern, "TAGNAME", "NAME", "TYPE", "LENGTH"));
			log.info(bar);
			map.entrySet().forEach(e-> 
			log.info(format(pattern, e.getKey(), e.getValue().getColumnName(), e.getValue().getDataType(), e.getValue().getDataSize())));
			log.info(bar);
		}
	}

}

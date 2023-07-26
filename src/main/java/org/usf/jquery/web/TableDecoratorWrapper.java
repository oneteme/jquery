package org.usf.jquery.web;

import static java.util.Optional.ofNullable;
import static org.usf.jquery.core.Utils.AUTO_TYPE;
import static org.usf.jquery.core.Utils.UNLIMITED;
import static org.usf.jquery.web.DatabaseScanner.database;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * 
 * @author u$f
 *
 */
public class TableDecoratorWrapper implements TableDecorator {

	final TableDecorator wrappedTable; //do not use Delegate
	final TableMetadata tableMetadata;
	
	public TableDecoratorWrapper(TableDecorator wrappedTable) {
		this.wrappedTable = wrappedTable;
		this.tableMetadata = new TableMetadata(tableName());
	}
	
	@Override
	public String identity() {
		return wrappedTable.identity();
	}

	@Override
	public String tableName() {
		return wrappedTable.tableName();
	}
	
	@Override
	public String columnName(ColumnDecorator cd) {
		return wrappedTable.columnName(cd);
	}

	@Override
	public int columnType(ColumnDecorator cd) {
		var type = wrappedTable.columnType(cd); //overridden
		if(type == AUTO_TYPE && tableMetadata.containsKey(cd)) {
			type = tableMetadata.get(cd).getDataType(); //db type
		}
		return type;
	}

	@Override
	public int columnSize(ColumnDecorator cd) {
		var size = wrappedTable.columnSize(cd); //overridden
		if(size == UNLIMITED && tableMetadata.containsKey(cd)) {
			size = tableMetadata.get(cd).getDataSize(); //db size
		}
		return size;
	}

	void fetch(DatabaseMetaData metadata) throws SQLException {
		tableMetadata.clear();
		var declaredCols = declaredColumns();
		try(var rs = metadata.getColumns(null, null, tableName(), null)){
			if(!rs.next()) {
				throw new NoSuchElementException(tableName() + " table not found");
			}
			do {
				var cn = rs.getString("COLUMN_NAME");
				if(declaredCols.containsKey(cn)) {
					tableMetadata.put(declaredCols.get(cn), new ColumnMetadata(cn,  
							rs.getInt("DATA_TYPE"), 
							rs.getInt("COLUMN_SIZE")));
				}
				//else undeclared column
			} while(rs.next());
		}
		tableMetadata.requireColumns(this, declaredCols.values());
	}
	
	/**
	 * key = columnName
	 */
	Map<String, ColumnDecorator> declaredColumns() {
		var dc = new LinkedHashMap<String, ColumnDecorator>();
		database().columns().stream()
		.map(ColumnDecoratorWrapper::unwrap)
		.forEach(cd-> 
			ofNullable(columnName(cd)) //should not throw any exception
			.ifPresent(cn-> dc.put(cn, cd)));
		return dc;
	}
}
